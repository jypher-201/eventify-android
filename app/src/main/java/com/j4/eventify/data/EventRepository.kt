package com.j4.eventify.data

import com.j4.eventify.data.local.EventDao
import com.j4.eventify.data.local.EventEntity
import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {

    // Reads the continuous stream of events from Room
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()

    suspend fun insertEvent(event: EventEntity) {
        eventDao.insertEvent(event)
    }

    suspend fun deleteEvent(event: EventEntity) {
        eventDao.deleteEvent(event)
    }
}