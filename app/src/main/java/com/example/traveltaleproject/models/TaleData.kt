package com.example.traveltaleproject.models

import android.os.Parcel
import android.os.Parcelable

data class TaleData(
    var talesid: String = "",
    var text: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(talesid)
        parcel.writeString(text)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TaleData> {
        override fun createFromParcel(parcel: Parcel): TaleData {
            return TaleData(parcel)
        }

        override fun newArray(size: Int): Array<TaleData?> {
            return arrayOfNulls(size)
        }
    }
}