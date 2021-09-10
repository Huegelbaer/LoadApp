package com.udacity

import android.os.Parcel
import android.os.Parcelable

class DownloadModel(
    val id: Long,
    val name: String?,
    val repository: String?,
    val status: Status,
    val url: String?,
    val fileUrl: String?
) : Parcelable {

    enum class Status {
        SUCCESS, FAIL, UNKNOWN
    }

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        Status.values()[parcel.readInt()],
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeString(repository)
        parcel.writeInt(status.ordinal)
        parcel.writeString(url)
        parcel.writeString(fileUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DownloadModel> {
        override fun createFromParcel(parcel: Parcel): DownloadModel {
            return DownloadModel(parcel)
        }

        override fun newArray(size: Int): Array<DownloadModel?> {
            return arrayOfNulls(size)
        }
    }
}