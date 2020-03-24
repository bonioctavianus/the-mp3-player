package com.bonioctavianus.android.themp3player.ui

import com.bonioctavianus.android.themp3player.base.Mp3Intent
import com.bonioctavianus.android.themp3player.model.Song

sealed class SongIntent : Mp3Intent {

    object LoadSongs : SongIntent()
    object ShowPlaylist : SongIntent()
    object ToggleReplay : SongIntent()
    data class ToggleShuffle(val status: Boolean) : SongIntent()
    data class TogglePlay(val song: Song) : SongIntent()
    data class SetPosition(val progress: Int) : SongIntent()
    data class RewindPosition(val time: Int) : SongIntent()
    data class ForwardPosition(val time: Int) : SongIntent()
    data class SwitchSong(val song: Song) : SongIntent()
    data class SelectSong(val song: Song) : SongIntent()
    object GetTrackInfo : SongIntent()
    object GetTrackDuration : SongIntent()
}
