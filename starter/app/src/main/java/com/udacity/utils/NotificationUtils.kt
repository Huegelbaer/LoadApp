package com.udacity.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.udacity.*

class NotificationUtils(
    private val notificationManager: NotificationManager,
    private val context: Context
) {

    companion object {
        private const val CHANNEL_ID = "downloadInformation"
        const val SHOW_DOWNLOAD_KEY = "download"
    }

    init {
        registerDownloadCompletedNotificationChannel()
    }

    private fun registerDownloadCompletedNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Download information",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createDownloadingNotification(
        id: Int,
        title: String,
        content: String,
        progress: Int = 0
    ) {

        val notification = createBaseNotificationBuilder(title, content)
            .setProgress(100, progress, true)
            .setOnlyAlertOnce(true)
            .build()

        notificationManager.notify(id, notification)
    }

    fun createDownloadCompletedNotification(
        id: Int,
        downloadModel: DownloadModel,
        title: String,
        content: String,
        actionTitle: String
    ) {

        val intent = Intent(context, DetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("Download", downloadModel)
        }
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = createBaseNotificationBuilder(title, content)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_assistant_black_24dp, actionTitle, pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .build()

        notificationManager.notify(id, notification)
    }

    fun createDownloadFailedNotification(
        id: Int,
        downloadModel: DownloadModel,
        title: String,
        content: String,
        retryActionTitle: String,
        showActionTitle: String
    ) {

        val retryPendingIntent = createRetryIntent()
        val showPendingIntent = createShowIntent(downloadModel)

        val notification = createBaseNotificationBuilder(title, content)
            .addAction(R.drawable.ic_assistant_black_24dp, retryActionTitle, retryPendingIntent)
            .addAction(R.drawable.ic_assistant_black_24dp, showActionTitle, showPendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .build()

        notificationManager.notify(id, notification)
    }

    private fun createBaseNotificationBuilder(
        title: String,
        content: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }

    private fun createShowIntent(downloadModel: DownloadModel): PendingIntent {
        val intent = Intent(context, DetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(SHOW_DOWNLOAD_KEY, downloadModel)
        }
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun createRetryIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}