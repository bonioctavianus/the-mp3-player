package com.bonioctavianus.android.themp3player.ui

import com.bonioctavianus.android.themp3player.base.Mp3ViewState
import com.bonioctavianus.android.themp3player.model.Media
import com.bonioctavianus.android.themp3player.model.Track
import com.bonioctavianus.android.themp3player.usecase.SongPlayer

data class SongViewState(
    val replay: Boolean,
    val shuffle: Boolean,
    val playing: Boolean,
    val durationInProgress: Boolean,
    val data: Media?,
    val playlistVisible: Boolean,
    val error: Throwable?
) : Mp3ViewState {

    companion object {
        fun default(): SongViewState {
            return SongViewState(
                replay = false,
                shuffle = false,
                playing = false,
                durationInProgress = false,
                data = null,
                playlistVisible = false,
                error = null
            )
        }
    }
}

sealed class SongPartialState : Mp3ViewState {

    object PlaylistVisible : SongPartialState()
    data class ReplayChanged(val status: Boolean) : SongPartialState()
    data class ShuffleChanged(val status: Boolean) : SongPartialState()
    object PositionChanged : SongPartialState()

    sealed class LoadSongs : SongPartialState() {
        object InFlight : LoadSongs()
        object Empty : LoadSongs()
        data class Loaded(val media: Media) : LoadSongs()
        data class Error(val throwable: Throwable) : LoadSongs()
    }

    object SongPlayed : SongPartialState()
    data class SongResumed(val status: SongPlayer.PlayerStatus) : SongPartialState()
    data class TrackLoaded(val track: Track) : SongPartialState()
    data class DurationLoaded(val currentDuration: String, val progress: Int) : SongPartialState()
}
