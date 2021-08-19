package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var _viewModel: MainViewModel

    private lateinit var downloadManager: DownloadManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))
            .get(MainViewModel::class.java)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        registerDownloadCompletedNotificationChannel()

        custom_button.setOnClickListener {
            download()
        }

        radio_group.setOnCheckedChangeListener { _, checkedId ->
            val source = mapRadioButtonToSource(checkedId)
            _viewModel.onDownloadSourceSelected(source)
        }

        _viewModel.shouldShowURLInput.observe(this, Observer {
            it?.let {
                if (it) {
                    edit_custom_url.visibility = View.VISIBLE
                    edit_custom_url.requestFocus()
                } else {
                    edit_custom_url.visibility = View.GONE
                }
            }
        })

        edit_custom_url.addTextChangedListener {
            _viewModel.onCustomUrlEntered(it.toString())
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            id?.let {
                createDownloadCompletedNotification(it)
            }
        }
    }

    private fun mapRadioButtonToSource(selectedId: Int): MainViewModel.DownloadSource? {
        return when (selectedId) {
            R.id.radio_button_glide -> MainViewModel.DownloadSource.GLIDE
            R.id.radio_button_load_app -> MainViewModel.DownloadSource.PROJECT
            R.id.radio_button_retrofit -> MainViewModel.DownloadSource.RETROFIT
            R.id.radio_button_custom_url -> MainViewModel.DownloadSource.CUSTOM_URL
            else -> null
        }
    }

    private fun download() {
        val url = _viewModel.downloadURL ?: return showNoFileSelectedToast()
        if (!_viewModel.isDownloadUrlValid()) return showInvalidURLToast()

        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.

        createDownloadingNotification(downloadID)


        // using query method
        var finishDownload = false
        var progress: Int
        loop@ while (!finishDownload) {
            var cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID));
            if (cursor.moveToFirst()) {
                var status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                when (status) {
                    DownloadManager.STATUS_FAILED -> {
                        finishDownload = true
                    }
                    DownloadManager.STATUS_PAUSED -> break@loop
                    DownloadManager.STATUS_PENDING -> break@loop
                    DownloadManager.STATUS_RUNNING -> {
                        var total = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        if (total >= 0) {
                            var downloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                            progress = ((downloaded * 100L) / total).toInt()
                            // if you use downloadmanger in async task, here you can use like this to display progress.
                            // Don't forget to do the division in long to get more digits rather than double.
                            //  publishProgress((int) ((downloaded * 100L) / total));
                            createDownloadingNotification(downloadID, progress)
                        }
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        progress = 100
                        // if you use aysnc task
                        // publishProgress(100);
                        finishDownload = true
                    }
                }
            }
        }
    }

    private fun getDownloadInfo(id: Long): DownloadModel {
        if (id < 0) {
            return DownloadModel(id, "Nix", DownloadModel.Status.FAIL)
        }

        val cursor = downloadManager.query(
            DownloadManager.Query().setFilterById(id)
        ) ?:return DownloadModel(id, "Nix", DownloadModel.Status.FAIL)

        if (!cursor.moveToFirst()) {
            return DownloadModel(id, "Nix", DownloadModel.Status.FAIL)
        }

        val statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val statusInt = cursor.getInt(statusColumn)

        val nameColumn = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
        val name = cursor.getString(nameColumn)

        cursor.close()

        val status = when (statusInt) {
            DownloadManager.STATUS_FAILED -> DownloadModel.Status.FAIL
            DownloadManager.STATUS_SUCCESSFUL -> DownloadModel.Status.SUCCESS
            else -> DownloadModel.Status.UNKNOWN
        }

        return DownloadModel(id, name, status)
    }


    private fun showNoFileSelectedToast() {
        val text = getText(R.string.no_file_selected_toast)
        Toast.makeText(application, text, Toast.LENGTH_LONG).show()
    }

    private fun showInvalidURLToast() {
        val text = getText(R.string.url_invalid_toast)
        Toast.makeText(application, text, Toast.LENGTH_LONG).show()
    }

    private fun registerDownloadCompletedNotificationChannel() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Download Completed", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createDownloadingNotification(id: Long, progress: Int = 0) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_download_running_description))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setProgress(100, progress, false)
            .build()

        notificationManager.notify(id.toInt(), notification)
    }

    private fun createDownloadCompletedNotification(id: Long) {
        val download = getDownloadInfo(id)

        val intent = Intent(applicationContext, DetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("Download", download)
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_download_complete_description))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_assistant_black_24dp, getString(R.string.notification_button), pendingIntent)
            .setAutoCancel(true)
            .setProgress(100, 100, false)
            .build()

        notificationManager.notify(id.toInt(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "channelId"
    }

}
