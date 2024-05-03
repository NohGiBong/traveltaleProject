package com.example.traveltaleproject.tale

import android.os.Parcel
import android.os.Parcelable

data class TaleData(
    var aid: String = "",
    var text: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(aid)
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