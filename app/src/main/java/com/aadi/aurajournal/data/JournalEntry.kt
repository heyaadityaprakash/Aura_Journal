package com.aadi.aurajournal.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
    val content:String,
    val timeStamp: Long= System.currentTimeMillis(),
    val mood:String?=null,
    val weatherContext:String?=null,
    val locationContext:String?=null,
)