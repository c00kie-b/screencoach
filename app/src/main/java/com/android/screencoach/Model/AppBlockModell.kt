package com.android.achievix.Model

import android.os.Parcel
import android.os.Parcelable

data class AppBlockModell(
    val appName: String,
    val packageName: String,
    val iconResId: Int,  // Resource ID of the Drawable
    val extra: Long,
    var blocked: Boolean,
    var isChecked: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(appName)
        parcel.writeString(packageName)
        parcel.writeInt(iconResId)
        parcel.writeLong(extra)
        parcel.writeByte(if (blocked) 1 else 0)
        parcel.writeByte(if (isChecked) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AppBlockModell> {
        override fun createFromParcel(parcel: Parcel): AppBlockModell {
            return AppBlockModell(parcel)
        }

        override fun newArray(size: Int): Array<AppBlockModell?> {
            return arrayOfNulls(size)
        }
    }
}
