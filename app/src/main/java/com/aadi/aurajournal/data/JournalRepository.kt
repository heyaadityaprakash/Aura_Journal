package com.aadi.aurajournal.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class JournalRepository(private val journalDao: JournalDao, private val context: Context) {

    private val prefs = context.getSharedPreferences("aura_journal_prefs", Context.MODE_PRIVATE)

    fun getUsername(): String {
        return prefs.getString("username", "User") ?: "User"
    }

    fun saveUsername(username: String) {
        prefs.edit().putString("username", username).apply()
    }

    //all entries

    fun getEntries(): Flow<List<JournalEntry>> = journalDao.getAllEntries()

    suspend fun insert(entry: JournalEntry){
        journalDao.insertEntry(entry)
    }

    suspend fun delete(entry: JournalEntry){
        journalDao.deleteEntry(entry)
    }
    suspend fun update(entry: JournalEntry){
        journalDao.updateWish(entry)
    }
}