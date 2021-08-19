package com.udacity

import android.webkit.URLUtil
import androidx.lifecycle.*

class MainViewModel(): ViewModel() {

    enum class DownloadSource {
        GLIDE, PROJECT, RETROFIT, CUSTOM_URL
    }

    private companion object {
        private const val GLIDE_URL = "https://github.com/bumptech/glide/archive/master.zip"
        private const val PROJECT_URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val RETROFIT_URL = "https://github.com/square/retrofit/archive/master.zip"
    }


    private var _source: DownloadSource? = null
    private var _customURL: String? = null

    private var _selectedSource = MutableLiveData<DownloadSource>()
    val selectedSource: LiveData<DownloadSource>
        get() = _selectedSource

    val shouldShowURLInput: LiveData<Boolean> = Transformations.map(_selectedSource) { source ->
        source == DownloadSource.CUSTOM_URL
    }

    val downloadURL: String?
        get() {
            return when (_source) {
                DownloadSource.GLIDE -> GLIDE_URL
                DownloadSource.PROJECT -> PROJECT_URL
                DownloadSource.RETROFIT -> RETROFIT_URL
                DownloadSource.CUSTOM_URL -> _customURL
                else -> null
            }
        }

    fun onDownloadSourceSelected(source: DownloadSource?) {
        _source = source
        _selectedSource.value = source
    }

    fun onCustomUrlEntered(url: String) {
        _customURL = url
        _enteredCustomURL.value = url
    }

}