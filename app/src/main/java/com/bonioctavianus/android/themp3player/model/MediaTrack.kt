package com.bonioctavianus.android.themp3player.model

import com.bonioctavianus.android.themp3player.usecase.SongPlayer

data class Media(
    val songs: List<Song>,
    val track: Track
)

data class Track(
    val song: Song,
    val replay: Boolean,
    val shuffle: Boolean,
    val status: SongPlayer.PlayerStatus,
    val currentDuration: String,
    val totalDuration: String,
    val progress: Int
)
