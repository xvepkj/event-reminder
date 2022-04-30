package com.example.event_reminder.util

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.event_reminder.MainActivity
import com.example.event_reminder.R
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    private lateinit var mNotificationManager: NotificationManager
    private val NOTIFICATION_ID = 0

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NOTIFICATION", "Received")
        //Setting daily notification recursively
        val alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        val contentIntent = Intent(context, AlarmReceiver::class.java)
        val contentPendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val cal = getTodayCalender(9,0)
        Log.d("NOTIFICATION", "${cal.timeInMillis},${Calendar.getInstance().timeInMillis}")
        alarmManager.setWindow(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            1000,
            contentPendingIntent
        )
        mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        deliverNotification(context, intent)
    }


    private fun deliverNotification(context: Context, receivedIntent: Intent) {
        val NOTIFICATION_ID = receivedIntent.getIntExtra("id", 0)
        val PRIMARY_CHANNEL_ID = context.getString(R.string.default_channel_id)
        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
            .setSmallIcon(R.drawable.img_birthday)
            .setContentTitle("Daily Events")
            .setContentText(context.getString(R.string.notification_content_text))
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * Returns today's Calender with given hour and minute.
     */
    private fun getTodayCalender(hour : Int,minute : Int) : Calendar{
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        if (cal.before(Calendar.getInstance())) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal
    }
}