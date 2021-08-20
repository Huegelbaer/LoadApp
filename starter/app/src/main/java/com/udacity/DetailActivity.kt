package com.udacity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
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
            downloadModel?.let {
                if (it.status == DownloadModel.Status.SUCCESS) {

                }
            }
        }
    }

    private fun handleDownloadIntent(intent: Intent) {
        intent.extras?.getParcelable<DownloadModel>("Download")?.let { download ->
            downloadModel = download
            fileNameValue.text = download.name
            statusValue.text = when(download.status) {
                DownloadModel.Status.SUCCESS -> "SUCCESS"
                DownloadModel.Status.FAIL -> "FAIL"
                DownloadModel.Status.UNKNOWN -> "UNKNOWN"
            }
        }
    }
}
