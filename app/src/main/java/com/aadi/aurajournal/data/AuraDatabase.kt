package com.aadi.aurajournal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [JournalEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AuraDatabase: RoomDatabase(){
    abstract fun journalDao(): JournalDao


//    Singleton instance of the Room database so that only one database object exists in the entire app.
    companion object{
        @Volatile
        private var INSTANCE:AuraDatabase?=null

        fun getDatabase(context: Context): AuraDatabase{
            return INSTANCE?:synchronized (this) {
                val instance=Room.databaseBuilder(
                    context.applicationContext,
                    AuraDatabase::class.java,
                    "aura_journal_database"
                ).build()
                INSTANCE=instance
                instance
            }
        }
    }

}


