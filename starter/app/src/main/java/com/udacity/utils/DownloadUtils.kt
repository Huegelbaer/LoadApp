package com.udacity.utils

import android.app.DownloadManager
import android.net.Uri
import com.udacity.DownloadModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class DownloadUtils(private val downloadManager: DownloadManager) {

    class DownloadStatus(var status: Status, var progress: Int? = null)

    enum class Status {
        RUNNING, PAUSED, PENDING, FAILED, SUCCESS
    }

    fun getInfo(downloadId: Long, url: String? = null): DownloadModel {
        if (downloadId < 0) {
            return DownloadModel(downloadId, null, DownloadModel.Status.FAIL, url)
        }

        val cursor = downloadManager.query(
            DownloadManager.Query().setFilterById(downloadId)
        ) ?:return DownloadModel(downloadId, null, DownloadModel.Status.FAIL, url)

        if (!cursor.moveToFirst()) {
            return DownloadModel(downloadId, null, DownloadModel.Status.FAIL, url)
        }

        val statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val statusInt = cursor.getInt(statusColumn)

        val nameColumn = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
        val name = cursor.getString(nameColumn)

        val uriColumn = cursor.getColumnIndex(DownloadManager.COLUMN_URI)
        val uri = cursor.getString(uriColumn)

        cursor.close()

        val status = when (statusInt) {
            DownloadManager.STATUS_FAILED -> DownloadModel.Status.FAIL
            DownloadManager.STATUS_SUCCESSFUL -> DownloadModel.Status.SUCCESS
            else -> DownloadModel.Status.UNKNOWN
        }

        return DownloadModel(downloadId, name, status, uri)
    }

    fun startDownload(uri: Uri, title: String, description: String): Long {
        val request =
            DownloadManager.Request(uri)
                .setTitle(title)
                .setDescription(description)
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        return downloadManager.enqueue(request) // enqueue puts the download request in the queue.
    }

    fun observeDownload(downloadID: Long): Flow<DownloadStatus> = flow {
        var finishDownload = false
        var progress = 0.0
        var downloadStatus = DownloadStatus(Status.PENDING)

        while (!finishDownload) {
            val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
            if (cursor.moveToFirst()) {
                when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                    DownloadManager.STATUS_FAILED -> {
                        downloadStatus = DownloadStatus(Status.FAILED)
                        finishDownload = true
                    }
                    DownloadManager.STATUS_PAUSED -> downloadStatus = DownloadStatus(Status.PAUSED)
                    DownloadManager.STATUS_PENDING -> downloadStatus = DownloadStatus(Status.PENDING)
                    DownloadManager.STATUS_RUNNING -> {
                        val total = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        if (total >= 0) {
                            val downloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                            progress = downloaded.toDouble()/ total.toDouble() * 100L
                        }
                        downloadStatus = DownloadStatus(Status.RUNNING, progress.toInt())
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        downloadStatus = DownloadStatus(Status.SUCCESS)
                        finishDownload = true
                    }
                }
                emit(downloadStatus)
            }
            cursor.close()
        }
    }
}