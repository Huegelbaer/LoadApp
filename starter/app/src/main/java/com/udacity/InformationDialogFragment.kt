package com.udacity

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class InformationDialogFragment(private val downloadModel: DownloadModel) : DialogFragment() {

    var listener: NoticeDialogListener? = null

    interface NoticeDialogListener {
        fun onDialogButtonClick(dialog: DialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val inflater = requireActivity().layoutInflater
            val dialogView = inflater.inflate(R.layout.fragment_informtion_dialog, null)
            val dialog = AlertDialog.Builder(it)
                .setTitle(R.string.dialog_title_information)
                .setView(dialogView)
                .setNeutralButton(R.string.open_file_button) { dialog, _ ->
                    listener?.onDialogButtonClick(this)
                    dialog.cancel()
                }

            val valueFilenameLabel = dialogView.findViewById<TextView>(R.id.valueFilename)
            valueFilenameLabel.text = downloadModel.name
            val valueRepositoryLabel = dialogView.findViewById<TextView>(R.id.valueRepository)
            valueRepositoryLabel.text = downloadModel.repository
            val valueFilePathLabel = dialogView.findViewById<TextView>(R.id.valueFilePath)
            valueFilePathLabel.text = downloadModel.fileUrl
            val valueLinkLabel = dialogView.findViewById<TextView>(R.id.valueLink)
            valueLinkLabel.text = downloadModel.url

            dialog.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }

}