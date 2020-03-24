package com.bonioctavianus.android.themp3player.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Song(
    val id: Long,
    val title: String,
    val artist: String
) : Parcelable {

    fun isDefault(): Boolean = id == -1L

    companion object {
        fun default(): Song {
            return Song(
                id = -1,
                title = "Unknown",
                artist = "Unknown"
            )
        }
    }
}