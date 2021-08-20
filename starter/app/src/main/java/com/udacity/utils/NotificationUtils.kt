package com.udacity.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.udacity.DetailActivity
import com.udacity.DownloadModel
import com.udacity.R

class NotificationUtils(
    private val notificationManager: NotificationManager,
    private val context: Context)
{

    companion object {
        private const val CHANNEL_ID = "channelId"
    }

    init {
        registerDownloadCompletedNotificationChannel()
    }

    private fun registerDownloadCompletedNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Download Completed", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createDownloadingNotification(
        id: Int,
        title: String,
        content: String,
        progress: Int = 0) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setProgress(100, progress, true)
            .build()

        notificationManager.notify(id, notification)
    }

    fun createDownloadCompletedNotification(
        id: Int,
        downloadModel: DownloadModel,
        title: String,
        content: String,
        actionTitle: String) {

        val intent = Intent(context, DetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("Download", downloadModel)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_assistant_black_24dp, actionTitle, pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }

    fun createDownloadFailedNotification(
        id: Int,
        downloadModel: DownloadModel,
        title: String,
        content: String,
        actionTitle: String) {

        val intent = Intent(context, DetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("Download", downloadModel)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_assistant_black_24dp, actionTitle, pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }

}