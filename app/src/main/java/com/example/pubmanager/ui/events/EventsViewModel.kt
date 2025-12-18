package com.example.pubmanager.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pubmanager.data.model.Event
import com.example.pubmanager.domain.mapper.toUi
import com.example.pubmanager.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {

    private val _events = MutableStateFlow<List<EventUi>>(emptyList())
    val events: StateFlow<List<EventUi>> = _events.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            val dbEvents = repository.getAllEvents()
            _events.value = dbEvents.map { it.toUi() }
        }
    }

    fun addEvent(name: String, date: LocalDate) {
        viewModelScope.launch {
            val event = Event(
                id = 0L,
                name = name,
                date = date
            )
            repository.insertEvent(event)
            loadEvents()
        }
    }

    fun updateEvent(id: Long, name: String, date: LocalDate) {
        viewModelScope.launch {
            val event = Event(
                id = id,
                name = name,
                date = date
            )
            repository.updateEvent(event)
            loadEvents()
        }
    }

    fun deleteEvent(id: Long) {
        viewModelScope.launch {
            val uiEvent = _events.value.firstOrNull { it.id == id } ?: return@launch
            val event = Event(
                id = uiEvent.id,
                name = uiEvent.name,
                date = uiEvent.date
            )
            repository.deleteEvent(event)
            loadEvents()
        }
    }
}
