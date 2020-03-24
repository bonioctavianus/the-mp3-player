package com.bonioctavianus.android.themp3player.service

import android.content.Intent
import com.bonioctavianus.android.themp3player.Mp3App
import com.bonioctavianus.android.themp3player.model.Song
import javax.inject.Inject

class ServiceRunner @Inject constructor(
    private val mApplication: Mp3App
) {

    companion object {
        const val KEY_SONG = "song"
        const val KEY_FORCE_PLAY = "force_play"
    }

    fun startSongService(song: Song, forcePlay: Boolean = false) {
        mApplication.startService(
            Intent(mApplication, SongService::class.java)
                .apply {
                    putExtra(KEY_SONG, song)
                    putExtra(KEY_FORCE_PLAY, forcePlay)
                }
        )
    }
}
