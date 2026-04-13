package com.aadi.aurajournal.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class JournalRepository(
    private val journalDao: JournalDao,
    private val firestoreManager: FirestoreManager,
    private val context: Context,
    private val externalScope: CoroutineScope
) {

    private val prefs = context.getSharedPreferences("aura_journal_prefs", Context.MODE_PRIVATE)

    fun getUsername(): String {
        return prefs.getString("username", "User") ?: "User"
    }

    fun saveUsername(username: String) {
        prefs.edit().putString("username", username).apply()
    }

    fun isAppLockEnabled(): Boolean {
        return prefs.getBoolean("app_lock_enabled", false)
    }

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("app_lock_enabled", enabled).apply()
    }

    //all entries
    fun getEntries(): Flow<List<JournalEntry>> = journalDao.getAllEntries()

    suspend fun insert(entry: JournalEntry) {
        // Save locally first for instant UI update
        val rowId = journalDao.insertEntry(entry)
        
        // If it was a new entry (id was 0), Room generates a new ID.
        // We must sync that NEW ID to Firestore, otherwise subsequent updates/deletes won't match.
        val entryToSync = if (entry.id == 0) {
            entry.copy(id = rowId.toInt())
        } else {
            entry
        }
        
        // Sync to cloud in the background scope
        externalScope.launch {
            firestoreManager.saveEntryToCloud(entryToSync)
        }
    }

    suspend fun delete(entry: JournalEntry) {
        // Capture the ID before local deletion to ensure it's available for cloud sync
        val entryId = entry.id
        
        // Delete locally
        journalDao.deleteEntry(entry)
        
        // Sync to cloud in the background scope using the captured ID
        externalScope.launch {
            firestoreManager.deleteEntryFromCloud(entryId)
        }
    }

    suspend fun update(entry: JournalEntry) {
        // Update locally
        journalDao.updateWish(entry)
        
        // Sync to cloud in the background scope
        externalScope.launch {
            firestoreManager.saveEntryToCloud(entry)
        }
    }
    
    // Optional: Add a method to trigger a full sync if needed
    suspend fun syncFromCloud() {
        val cloudEntries = firestoreManager.fetchAllEntriesFromCloud()
        if (cloudEntries.isNotEmpty()) {
            cloudEntries.forEach { entry ->
                journalDao.insertEntry(entry)
            }
        }
    }
}
