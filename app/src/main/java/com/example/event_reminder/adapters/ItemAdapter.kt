package com.example.event_reminder.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.recyclerview.widget.RecyclerView
import com.example.event_reminder.Event
import com.example.event_reminder.R
import java.util.*

class ItemAdapter(private val context: Context, private val dataset: List<Event>) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    var onItemClick: ((Event) -> Unit)? = null

    inner class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.txt_name)
        val dateTextView: TextView = view.findViewById(R.id.txt_date)
        val ageTextView: TextView = view.findViewById(R.id.txt_age)
        val eventImageView: ImageView = view.findViewById(R.id.imv_event)

        init {
            view.setOnClickListener {
                onItemClick?.invoke(dataset[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_list_item, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val event = dataset[position]
        holder.apply {
            nameTextView.text = event.name
            //getting Date in correct format
            dateTextView.text = getDateString(event.day!!, event.month!!, event.year!!)
            //calculating age according to event's day,month & year
            ageTextView.text = calculateAge(event.day!!, event.month!! - 1, event.year!!)
            //Setting eventImageView according to the event type
            eventImageView.setImageDrawable(getEventDrawable(event.eventType!!))
        }
    }

    override fun getItemCount() = dataset.size

    /**
     * Returns Age of the event in Y.O., M.O. or D.O. according to the difference.
     */
    private fun calculateAge(day: Int, month: Int, year: Int): String {
        val today: Calendar = Calendar.getInstance()
        val birthDate: Calendar = Calendar.getInstance()
        birthDate.set(year, month, day)
        var age: Int = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
        //if current month is before the birth month then decrease the year
        if (today.get(Calendar.MONTH) < birthDate.get(Calendar.MONTH))
            age--
        //if the current month and birth month are the same and current date is before birth date
        else if (today.get(Calendar.MONTH) == birthDate.get(Calendar.MONTH) &&
            today.get(Calendar.DAY_OF_MONTH) < birthDate.get(Calendar.DAY_OF_MONTH)
        )
            age--
        //if the event is 0 Y.O.
        return if (age == 0) {
            // calculate month difference between current month and event month
            val monthDiff = (today.get(Calendar.MONTH) - birthDate.get(Calendar.MONTH) + 12) % 12
            if (monthDiff == 0) {
                //calculate date difference between today and event date
                (today.get(Calendar.DAY_OF_MONTH) - birthDate.get(Calendar.DAY_OF_MONTH)).toString() + context.getString(
                    R.string.arg_old,
                    " days"
                )
            } else monthDiff.toString() + context.getString(R.string.arg_old, " months")
        } else age.toString() + context.getString(R.string.arg_old, " years")
    }

    /**
     * Returns "Today" if the event dateTextView is of today and returns dateTextView is correct format otherwise.
     */
    private fun getDateString(day: Int, month: Int, year: Int): String {
        val today: Calendar = Calendar.getInstance()
        return if (year == today.get(Calendar.YEAR) && month - 1 == today.get(Calendar.MONTH)
            && day == today.get(Calendar.DAY_OF_MONTH)
        )
            context.getString(R.string.today)
        else "${day}-${month}-${year}"
    }

    /**
     * Returns Drawable according to event type
     */
    private fun getEventDrawable(eventType: Int): Drawable? {
        return if (eventType == 1)
            getDrawable(context, R.drawable.img_anniversary)
        else
            getDrawable(context, R.drawable.img_birthday)
    }
}