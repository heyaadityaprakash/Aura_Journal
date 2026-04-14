package com.aadi.aurajournal.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao{

//    get all entries
    @Query("SELECT * FROM journal_entries ORDER BY timeStamp DESC ")
    fun getAllEntries(): Flow<List<JournalEntry>>

//    insert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry): Long

//    update
    @Update
    suspend fun updateWish(entry: JournalEntry)

//    delete
    @Delete
    suspend fun deleteEntry(entry: JournalEntry)

    // Check if there's any entry between midnight today and midnight tomorrow
    @Query("SELECT COUNT(*) FROM journal_entries WHERE timeStamp >= :startOfDay AND timeStamp < :endOfDay")
    suspend fun getEntryCountForDateRange(startOfDay: Long, endOfDay: Long): Int
}
