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
import com.j4.eventify.data.remote.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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

    fun syncHolidaysFromApi(year: Int = 2026, countryCode: String = "PH") {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Check if we already downloaded them to save data
                // (Assuming you have a function in your repository to check this)
                // val existingHolidays = repository.getEventsByTypeSync("HOLIDAY")
                // if (existingHolidays.isNotEmpty()) return@launch

                // 2. Call the internet API!
                val apiHolidays = RetrofitClient.holidayApi.getHolidays(year, countryCode)

                // 3. Setup our time tools
                val manilaZone = TimeZone.getTimeZone("Asia/Manila")
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = manilaZone }

                // 4. Convert the Internet data into Database data and save it
                apiHolidays.forEach { apiHoliday ->
                    val timestamp = format.parse(apiHoliday.date)?.time ?: return@forEach

                    val holidayEntity = EventEntity(
                        title = apiHoliday.localName,
                        description = "Philippine Holiday",
                        eventType = "HOLIDAY",
                        timestamp = timestamp,
                        endTimestamp = timestamp + 86400000L, // Adds 24 hours for the end time
                        locationName = null, // <--- Fixed!
                        latitude = null,     // <--- Fixed!
                        longitude = null,    // <--- Fixed!
                        gradientIndex = 0,   // <--- Fixed!
                        customLabel = null,  // <--- Fixed!
                        isAllDay = true,
                        repeatMode = "Every year",
                        remindBeforeMinutes = emptyList()
                    )

                    // Insert into your Room DB!
                    // repository.insert(holidayEntity)
                }
            } catch (e: Exception) {
                // The internet is down, or the API is broken.
                // That's fine! We just fail silently and the app keeps working normally.
                e.printStackTrace()
            }
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