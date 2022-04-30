package com.example.event_reminder

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.viewpager2.widget.ViewPager2
import com.example.event_reminder.adapters.MonthAdapter
import com.example.event_reminder.util.ActionType
import com.example.event_reminder.util.AlarmReceiver
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.realm.Realm
import java.util.*

class MainActivity : AppCompatActivity() {
    private val months =
        arrayOf("jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec")
    var calendar: Calendar = Calendar.getInstance()
    private val notificationKey: String = "notification"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Realm.init(this)

        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)

        val adapter = MonthAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter
        //setting up tab layout with viewpager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = months[position]
        }.attach()
        //setting current tab according to current month
        tabLayout.getTabAt(calendar.get(Calendar.MONTH))?.select()
        //creating notification channel
        createChannel("channel", "Daily")

        if (!isNotificationSet())
            setupDailyNotification()
    }

    /**
     * Create Notification Channel with given channel Id, channel name
     */
    private fun createChannel(channelId: String, channelName: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "For Daily Reminder"

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun setupDailyNotification() {
        setNotification()
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val contentIntent = Intent(applicationContext, AlarmReceiver::class.java)
        val contentPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val cal = getTodayCalender(9, 0)
        alarmManager.setWindow(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            1000,
            contentPendingIntent
        )
    }

    /**
     * Check if daily notification is set
     */
    private fun isNotificationSet() =
        getPreferences(Context.MODE_PRIVATE).getBoolean(notificationKey, false)

    private fun setNotification() {
        with(getPreferences(Context.MODE_PRIVATE).edit()) {
            putBoolean(notificationKey, true)
            apply()
        }
    }

    /**
     * Returns today's Calender with given hour and minute.
     */
    private fun getTodayCalender(hour: Int, minute: Int): Calendar {
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