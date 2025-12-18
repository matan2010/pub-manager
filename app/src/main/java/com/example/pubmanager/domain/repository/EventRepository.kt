package com.example.pubmanager.domain.repository

import com.example.pubmanager.data.dao.EventDao
import com.example.pubmanager.data.model.Event
import java.time.LocalDate

class EventRepository(
    private val eventDao: EventDao
) {

    suspend fun createEvent(
        name: String,
        date: LocalDate
    ): Long {
        val event = Event(
            id = 0L,
            name = name,
            date = date
        )
        return eventDao.insertEvent(event)
    }

    suspend fun insertEvent(event: Event): Long {
        return eventDao.insertEvent(event)
    }

    suspend fun getAllEvents(): List<Event> {
        return eventDao.getAllEvents()
    }

    suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event)
    }

    suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event)
    }
}