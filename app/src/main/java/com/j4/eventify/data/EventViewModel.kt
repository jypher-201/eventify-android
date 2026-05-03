package com.j4.eventify.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.j4.eventify.EventifyApplication
import com.j4.eventify.data.local.EventEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventViewModel(private val repository: EventRepository) : ViewModel() {

    // Automatically updates the UI when the database changes
    val allEvents: StateFlow<List<EventEntity>> = repository.allEvents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addEvent(event: EventEntity, context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            // ── THIS IS THE CRITICAL LINE ──
            val generatedId = repository.insertEvent(event).toInt()

            // Trigger the Notification Scheduler!
            val savedEvent = event.copy(id = generatedId)
            com.j4.eventify.EventAlarmScheduler(context).schedule(savedEvent)
        }
    }

    fun updateEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }
}

// The Factory that builds your ViewModel
val EventViewModelFactory = viewModelFactory {
    initializer {
        val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as EventifyApplication)
        EventViewModel(application.repository)
    }
}