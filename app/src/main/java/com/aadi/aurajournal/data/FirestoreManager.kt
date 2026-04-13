package com.aadi.aurajournal.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreManager {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    private val currentUserId: String?
        get() = auth.currentUser?.uid


    //get user's entry only
    private fun getEntriesCollection() = currentUserId?.let {
        uid->
        firestore.collection("users").document(uid).collection("entries")
    }


    //save from room to firestore
    suspend fun saveEntryToCloud(entry: JournalEntry) {
        val collection = getEntriesCollection()?:return

        //convert room entity to Map (firestore understands map)

        val entryMap = hashMapOf(
            "id" to entry.id,
            "content" to entry.content,
            "timeStamp" to entry.timeStamp, // Fixed capitalization
            "mood" to entry.mood,
            "weatherContext" to entry.weatherContext,
            "locationContext" to entry.locationContext,
            "images" to entry.images
        )

        try {
            collection.document(entry.id.toString())
                .set(entryMap)
                .await()
            Log.d("FirestoreManager", "Entry ${entry.id} synced to cloud successfully!")
        }catch (e: Exception){
            Log.e("FirestoreManager", "Failed to sync entry to cloud", e)
        }
    }

//    delete entry from cloud

    suspend fun deleteEntryFromCloud(entryId:Int){
        val collection = getEntriesCollection()?:return

        try {
            collection.document(entryId.toString()).delete().await()
            Log.d("FirestoreManager", "Entry $entryId deleted from cloud")

        }catch (e: Exception){
            Log.e("FirestoreManager", "Failed to delete entry from cloud", e)

        }
    }

    //Pulls all entries from Firestore and converts them back to Room entities.

    suspend fun fetchAllEntriesFromCloud(): List<JournalEntry> {
        val collection = getEntriesCollection() ?: return emptyList()

        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { doc ->
                // Ensure these match the exact fields in your JournalEntry data class!
                val id = doc.getLong("id")?.toInt() ?: return@mapNotNull null
                val content = doc.getString("content") ?: ""
                val timeStamp = doc.getLong("timeStamp") ?: 0L
                val mood = doc.getString("mood")
                val weatherContext = doc.getString("weatherContext")
                val locationContext = doc.getString("locationContext")
                // Firestore stores lists as generic arrays, we must cast it safely
                val images = (doc.get("images") as? List<String>) ?: emptyList()

                JournalEntry(
                    id = id,
                    content = content,
                    timeStamp = timeStamp,
                    mood = mood,
                    weatherContext = weatherContext,
                    locationContext = locationContext,
                    images = images
                )
            }
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Failed to fetch entries", e)
            emptyList()
        }
    }

}