package com.udacity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.udacity.utils.NotificationUtils
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import java.io.File

class DetailActivity : AppCompatActivity(), InformationDialogFragment.NoticeDialogListener {

    private var downloadModel: DownloadModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        handleDownloadIntent(intent)

        fab.setOnClickListener {
            showInformation()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        return true
    }

    override fun onDialogButtonClick(dialog: DialogFragment) {
        handleOpenFileButtonClicked()
    }

    private fun handleDownloadIntent(intent: Intent) {
        intent.extras?.getParcelable<DownloadModel>(NotificationUtils.SHOW_DOWNLOAD_KEY)
            ?.let { download ->
                downloadModel = download
                fileNameValue.text = download.name
                statusValue.text = when (download.status) {
                    DownloadModel.Status.SUCCESS -> getString(R.string.download_state_completed)
                    DownloadModel.Status.FAIL -> getString(R.string.download_state_failed)
                    DownloadModel.Status.UNKNOWN -> getString(R.string.download_state_unknown)
                }
            }
    }

    private fun handleOpenFileButtonClicked() {
        downloadModel?.let { download ->
            if (download.status == DownloadModel.Status.SUCCESS) {
                download.fileUrl?.let { fileUrl ->
                    openFile(fileUrl)
                }
            }
        }
    }

    private fun showInformation() {
        downloadModel?.let { model ->
            val dialog = InformationDialogFragment(model)
            dialog.listener = this
            dialog.show(supportFragmentManager, "InformationDialog")
        }
    }

    private fun openFile(fileUrl: String) {
        val fileUrlWithoutSchema = fileUrl.removePrefix("file://")
        val file = File(fileUrlWithoutSchema)

        val uri = FileProvider.getUriForFile(
            applicationContext,
            "${BuildConfig.APPLICATION_ID}.fileProvider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW)
            .setData(uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(
                Intent.createChooser(
                    intent,
                    resources.getString(R.string.open_file_button)
                )
            )
        } catch (e: Exception) {
            Toast
                .makeText(applicationContext, R.string.can_not_open_file, Toast.LENGTH_SHORT)
                .show()
        }
    }
}
