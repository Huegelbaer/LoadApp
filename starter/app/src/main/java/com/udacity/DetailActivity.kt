package com.udacity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.udacity.utils.NotificationUtils
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import java.io.File

class DetailActivity : AppCompatActivity() {

    private var downloadModel: DownloadModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        handleDownloadIntent(intent)

        openFileButton.setOnClickListener {
            downloadModel?.let { it ->
                if (it.status == DownloadModel.Status.SUCCESS) {
                    it.fileUrl?.let { fileUrl ->
                        val url = fileUrl.removePrefix("file://")
                        val file = File(url)
                        val uri = FileProvider.getUriForFile(
                            applicationContext,
                            "${BuildConfig.APPLICATION_ID}.fileProvider",
                            file)

                        val intent = Intent(Intent.ACTION_VIEW)
                            .setData(uri)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            val lastSegment = uri.lastPathSegment
                            val newUrl = url.removeSuffix(lastSegment!!)
                            val selectedUri = Uri.parse(newUrl)
                            val newIntent = Intent(Intent.ACTION_VIEW)
                                .setDataAndType(selectedUri, "resource/folder")
                            startActivity(newIntent)
                        }
                    }
                }
            }
        }
    }

    private fun handleDownloadIntent(intent: Intent) {
        intent.extras?.getParcelable<DownloadModel>(NotificationUtils.SHOW_DOWNLOAD_KEY)
            ?.let { download ->
                downloadModel = download
                fileNameValue.text = download.name
                statusValue.text = when (download.status) {
                    DownloadModel.Status.SUCCESS -> "SUCCESS"
                    DownloadModel.Status.FAIL -> "FAIL"
                    DownloadModel.Status.UNKNOWN -> "UNKNOWN"
                }
            }
    }
}
