package com.j4.eventify

import android.app.Application
import com.j4.eventify.data.EventRepository
import com.j4.eventify.data.local.EventDatabase

class EventifyApplication : Application() {
    // Database and repository are created lazily so they don't slow down app startup
    val database by lazy { EventDatabase.getDatabase(this) }
    val repository by lazy { EventRepository(database.eventDao()) }
}