package com.example.event_reminder

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class Event() : RealmObject() {
    var name: String = ""
    var month: Int? = null
    var year: Int? = null
    var day: Int? = null
    var eventType: Int? = null
}