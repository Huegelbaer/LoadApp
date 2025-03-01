package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.udacity.utils.DownloadUtils
import com.udacity.utils.NotificationUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class MainActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    private lateinit var _viewModel: MainViewModel

    private lateinit var downloadUtils: DownloadUtils
    private lateinit var notificationUtils: NotificationUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))
            .get(MainViewModel::class.java)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        custom_button.setOnClickListener {
            scope.launch {
                download()
            }
        }

        radio_group.setOnCheckedChangeListener { _, checkedId ->
            val source = mapRadioButtonToSource(checkedId)
            _viewModel.onDownloadSourceSelected(source)
        }

        _viewModel.shouldShowURLInput.observe(this, {
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

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationUtils = NotificationUtils(notificationManager, applicationContext)

        handleRetryIntent(intent)
    }

    private fun handleRetryIntent(intent: Intent) {
        intent.extras?.getString(NotificationUtils.DOWNLOAD_URL_KEY)?.let { url ->
            _viewModel.prepareRetryDownload(url)
            scope.launch {
                download()
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
        val url = _viewModel.downloadURL ?: return withContext(Dispatchers.Main) {
            showNoFileSelectedToast()
        }
        if (!_viewModel.isDownloadUrlValid()) return withContext(Dispatchers.Main) {
            showInvalidURLToast()
        }

        val fileName =  URLUtil.guessFileName(
            url,
            null,
            MimeTypeMap.getFileExtensionFromUrl(url)
        )

        val downloadID = downloadUtils.startDownload(
            Uri.parse(url),
            fileName,
            getString(R.string.app_description),
            "/repos/${_viewModel.sourceName}/$fileName"
        )

        val repository = getString(_viewModel.repositoryName ?: -1)
        createDownloadingNotification(downloadID, repository)

        downloadUtils.observeDownload(downloadID).collect {
            withContext(Dispatchers.Main) {
                when (it.status) {
                    DownloadUtils.Status.RUNNING -> {
                        createDownloadingNotification(downloadID, repository, it.progress ?: 0)
                    }
                    DownloadUtils.Status.FAILED -> {
                        createDownloadFailedNotification(downloadID, url, repository)
                    }
                    DownloadUtils.Status.SUCCESS -> {
                        createDownloadCompletedNotification(downloadID, repository)
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

    private fun createDownloadingNotification(id: Long, repository: String?, progress: Int = 0) {
        val download = downloadUtils.getInfo(id, repository)

        notificationUtils.createDownloadingNotification(
            id.toInt(),
            getString(R.string.notification_title),
            getString(R.string.notification_download_running_description, download.name, repository),
            progress
        )
    }

    private fun createDownloadCompletedNotification(id: Long, repository: String?) {
        val download = downloadUtils.getInfo(id, repository)

        notificationUtils.createDownloadCompletedNotification(
            id.toInt(),
            download,
            getString(R.string.notification_title),
            getString(R.string.notification_download_complete_description, download.name, repository),
            getString(R.string.notification_button)
        )
    }

    private fun createDownloadFailedNotification(id: Long, url: String, repository: String?) {
        val download = downloadUtils.getInfo(id, repository, url)

        notificationUtils.createDownloadFailedNotification(
            id.toInt(),
            download,
            url,
            getString(R.string.notification_title),
            getString(R.string.notification_download_failed_description, download.name, repository),
            getString(R.string.notification_retry_button),
            getString(R.string.notification_button_failed)
        )
    }
}
