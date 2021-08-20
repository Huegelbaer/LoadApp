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
import com.udacity.utils.DownloadUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect


class MainActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    private var downloadID: Long = 0

    private lateinit var _viewModel: MainViewModel

    private lateinit var downloadUtils: DownloadUtils
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
            scope.launch {
                download()
            }
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

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadUtils = DownloadUtils(downloadManager)
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

    private suspend fun download() {
        val url = _viewModel.downloadURL ?: return showNoFileSelectedToast()
        if (!_viewModel.isDownloadUrlValid()) return showInvalidURLToast()

        downloadID = downloadUtils.startDownload(
            Uri.parse(url),
            getString(R.string.app_name),
            getString(R.string.app_description)
        )
        createDownloadingNotification(downloadID)

        downloadUtils.observeDownload(downloadID).collect {
            withContext(Dispatchers.Main) {
                when (it.status) {
                    DownloadUtils.Status.RUNNING -> {
                        createDownloadingNotification(downloadID, it.progress ?: 0)
                    }
                    DownloadUtils.Status.FAILED -> {
                        createDownloadCompletedNotification(downloadID)
                    }
                    DownloadUtils.Status.SUCCESS -> {
                        createDownloadCompletedNotification(downloadID)
                    }
                    else -> { }
                }
            }
        }
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
            .setProgress(100, progress, true)
            .build()

        notificationManager.notify(id.toInt(), notification)
    }

    private fun createDownloadCompletedNotification(id: Long) {
        val download = downloadUtils.getInfo(id)

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
            .build()

        notificationManager.notify(id.toInt(), notification)
    }

    companion object {
        private const val CHANNEL_ID = "channelId"
    }

}
