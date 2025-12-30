package com.example.pubmanager.ui.events

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import java.time.format.DateTimeFormatter

private val EVENT_DATE_FORMATTER =
    DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun EventsRoute(
    viewModel: EventsViewModel,
    onBackClick: () -> Unit,
    onOpenOrder: (eventId: Long, eventName: String, eventDate: java.time.LocalDate) -> Unit
) {
    val events by viewModel.events.collectAsState()

    EventsScreen(
        events = events,
        onBackClick = onBackClick,
        onSaveNewEvent = { name, date ->
            viewModel.addEvent(name, date)
        },
        onUpdateEvent = { id, name, date ->
            viewModel.updateEvent(id, name, date)
        },
        onDeleteEventClick = { id ->
            viewModel.deleteEvent(id)
        },
        onOpenOrder = { eventId, eventName, eventDate ->
            onOpenOrder(eventId, eventName, eventDate)  // ✅ רק מעביר הלאה
        }


    )
}