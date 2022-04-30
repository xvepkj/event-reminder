package com.example.event_reminder

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.event_reminder.util.ErrorType
import io.realm.Realm
import io.realm.RealmConfiguration
import java.util.*

class EventListViewModel : ViewModel() {
    //current tab's month
    var month: Int = 0
    private val config: RealmConfiguration = RealmConfiguration.Builder()
        .name("event : realm").build()
    private val realm: Realm = Realm.getInstance(config)

    fun addEvent(name: String, day: Int, month: Int, year: Int, eventType: Int) {
        realm.beginTransaction()
        val event = realm.createObject(Event::class.java)
        event.name = name.trim()
        event.day = day
        event.month = month
        event.year = year
        event.eventType = eventType
        realm.commitTransaction()
    }

    fun updateEvent(
        prevName: String,
        name: String,
        day: Int,
        month: Int,
        year: Int,
        eventType: Int
    ) {
        val event = realm.where(Event::class.java).equalTo("name", prevName).findFirst()
        realm.beginTransaction()
        event?.name = name.trim()
        event?.day = day
        event?.month = month
        event?.year = year
        event?.eventType = eventType
        realm.commitTransaction()
    }

    fun deleteEvent(event: Event) {
        val event = realm.where(Event::class.java).equalTo("name", event.name).findFirst()
        realm.beginTransaction()
        event?.deleteFromRealm()
        realm.commitTransaction()
    }

    /**
     * Returns list of events according to month
     */
    fun getEventList(): List<Event> {
        val notes = realm.where(Event::class.java).equalTo("month", month).findAll()
        val list: List<Event> = notes.where().findAll()
        return list
    }

    fun getEventListSize(): Int {
        return getEventList().size
    }

    fun getEventPosition(event: Event): Int {
        return getEventList().indexOf(event)
    }

    fun validateAddedEvent(
        name: String,
        day: Int,
        month: Int,
        year: Int,
        eventType: Int
    ): Pair<Boolean, ErrorType> {
        val event =
            realm.where(Event::class.java).equalTo("name", name.trim())
                .findFirst()
        var valid: Boolean = true
        var errorMessage: ErrorType = ErrorType.NO_ERROR
        val today: Calendar = Calendar.getInstance()
        val chosenDate: Calendar = Calendar.getInstance()
        chosenDate.set(year, month - 1, day)
        //check that event name should not be blank
        if (name.isBlank()) {
            valid = false
            errorMessage = ErrorType.NAME_BLANK
        //check that another event with the same name does not exist in database
        } else if (event != null) {
            valid = false
            errorMessage = ErrorType.NAME_UNIQUE
        //check that a date has been selected by the user
        } else if (day == 0) {
            valid = false
            errorMessage = ErrorType.CHOOSE_DATE
        //check that the date selected by the user is not a future date
        } else if (today < chosenDate) {
            valid = false
            errorMessage = ErrorType.DATE_IN_FUTURE
        //check that an event type has been selected by the user
        } else if (eventType == -1) {
            valid = false
            errorMessage = ErrorType.CHOOSE_EVENT_TYPE
        }
        return Pair(valid, errorMessage)
    }

    fun validateUpdatedEvent(
        prevName: String,
        name: String,
        day: Int,
        month: Int,
        year: Int,
        eventType: Int
    ): Pair<Boolean, ErrorType> {
        val event =
            realm.where(Event::class.java).equalTo("name", name.trim())
                .findFirst()
        var valid: Boolean = true
        var errorMessage: ErrorType = ErrorType.NO_ERROR
        val today: Calendar = Calendar.getInstance()
        val chosenDate: Calendar = Calendar.getInstance()
        chosenDate.set(year, month - 1, day)
        //check that event name should not be blank
        if (name.isBlank()) {
            valid = false
            errorMessage = ErrorType.NAME_BLANK
        //check that another event with the same name does not exist in database
        } else if (event != null && prevName
            != name.trim()
        ) {
            valid = false
            errorMessage = ErrorType.NAME_UNIQUE
        //check that the date selected by the user is not a future date
        } else if (today < chosenDate) {
            valid = false
            errorMessage = ErrorType.DATE_IN_FUTURE
        }
        return Pair(valid, errorMessage)
    }
}