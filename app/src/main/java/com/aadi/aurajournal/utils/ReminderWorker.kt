package com.aadi.aurajournal.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aadi.aurajournal.MainActivity
import com.aadi.aurajournal.data.AuraDatabase
import java.time.LocalDate
import java.time.ZoneId

class ReminderWorker (
    private val context: Context,
    workerParams: WorkerParameters
): CoroutineWorker(context,workerParams) {
    override suspend fun doWork(): Result {
        val database = AuraDatabase.getDatabase(context)
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val curentStreak = prefs.getInt("streak_count",0)

        //calculate today's timstamp
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        //check if user wrote today
        val entriesToday = database.journalDao().getEntryCountForDateRange(startOfDay,endOfDay)

        //if no entry today send noti

        if(entriesToday == 0){
            val title:String
            val message: String

            if(curentStreak > 0){
                title = "Don't break your streak! 🔥"
                message = "You're on a $curentStreak day streak. Take a minute to log your aura today."
            }else{
                title = "How was your day? ✨"
                message = "Capture your thoughts and let AI analyze your aura."
            }
            showNotification(title,message)
        }
        return Result.success()


    }



    private fun showNotification(title: String,message: String){
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "aura_daily_reminder"

        //build notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Journal Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds you to write your daily journal entry."
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Make the notification open the app when tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.mipmap.sym_def_app_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss when tapped
            .build()

        // Show it! (Using a static ID so it overwrites previous reminders rather than spamming)
        notificationManager.notify(1001, notification)
    }

}