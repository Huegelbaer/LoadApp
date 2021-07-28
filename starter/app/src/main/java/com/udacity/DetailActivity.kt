package com.udacity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        handleDownloadIntent(intent)
    }

    private fun handleDownloadIntent(intent: Intent) {
        intent.extras?.getParcelable<DownloadModel>("Download")?.let { download ->
            fileNameValue.text = download.name
            /*statusValue.text = when(download.status) {
                DownloadModel.Status.SUCCESS -> "SUCCESS"
                DownloadModel.Status.FAIL -> "FAIL"
                DownloadModel.Status.UNKNOWN -> "UNKNOWN"
            }*/
        }
    }
}
