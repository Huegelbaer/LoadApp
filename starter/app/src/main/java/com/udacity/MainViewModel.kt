package com.udacity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class MainViewModel(): ViewModel() {

    enum class DownloadSource {
        GLIDE, PROJECT, RETROFIT
    }

    private companion object {
        private const val GLIDE_URL = "https://github.com/bumptech/glide/archive/master.zip"
        private const val PROJECT_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val RETROFIT_URL = "https://github.com/square/retrofit/archive/master.zip"
    }

    private var _selectedSource = MutableLiveData<DownloadSource>()
    val selectedSource: LiveData<DownloadSource>
        get() = _selectedSource

    val downloadURL: LiveData<String?> = Transformations.map(_selectedSource) { source ->
        when (source) {
            DownloadSource.GLIDE -> GLIDE_URL
            DownloadSource.PROJECT -> PROJECT_URL
            DownloadSource.RETROFIT -> RETROFIT_URL
            else -> null
        }
    }

    fun onDownloadSourceSelected(source: DownloadSource) {
        _selectedSource.value = source
    }
}