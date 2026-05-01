package com.j4.eventify.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    // RETURNS Long: The ID of the newly inserted row
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    // RETURNS Int: The number of rows successfully deleted
    @Delete
    suspend fun deleteEvent(event: EventEntity): Int

    @Query("SELECT * FROM events_table ORDER BY timestamp ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    // RETURNS List<Long>: The IDs of all inserted holidays
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHolidays(holidays: List<EventEntity>): List<Long>
}