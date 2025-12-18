package com.example.pubmanager.ui.events

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun EventsRoute(
    viewModel: EventsViewModel,
    onBackClick: () -> Unit,
    onOpenOrder: (Long) -> Unit
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
        onOpenOrder = { eventId ->
            onOpenOrder(eventId)
        }
    )
}