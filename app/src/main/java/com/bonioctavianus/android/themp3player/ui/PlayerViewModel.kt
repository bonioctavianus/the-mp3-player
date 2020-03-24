package com.bonioctavianus.android.themp3player.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bonioctavianus.android.themp3player.base.BaseViewModel
import com.bonioctavianus.android.themp3player.usecase.SongPlayer
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class PlayerViewModel(
    private val mInteractor: PlayerInteractor
) : BaseViewModel<SongIntent, SongPartialState>() {

    val mState: MutableLiveData<SongViewState> = MutableLiveData()
    val mDurationState: MutableLiveData<SongViewState> = MutableLiveData()

    override fun bindIntent(intent: Observable<SongIntent>) {
        addDisposable(
            intent.compose(mInteractor.compose())
                .scan(SongViewState.default(), mReducer)
                .subscribe(
                    { value ->
                        if (!value.durationInProgress) {
                            mState.postValue(value)
                        } else {
                            mDurationState.postValue(value)
                        }
                    },
                    { }
                )
        )
    }

    private val mReducer =
        BiFunction { previousState: SongViewState, change: SongPartialState ->
            when (change) {
                is SongPartialState.PlaylistVisible -> {
                    previousState
                        .copy(
                            durationInProgress = false,
                            playlistVisible = previousState
                                .data?.songs?.isNullOrEmpty()?.not() == true
                        )
                }
                is SongPartialState.ReplayChanged -> {
                    previousState
                        .copy(
                            durationInProgress = false,
                            playlistVisible = false,
                            replay = change.status,
                            shuffle = if (change.status) false else previousState.shuffle
                        )
                }
                is SongPartialState.ShuffleChanged -> {
                    previousState
                        .copy(
                            durationInProgress = false,
                            playlistVisible = false,
                            replay = if (change.status) false else previousState.replay,
                            shuffle = change.status
                        )
                }
                is SongPartialState.PositionChanged -> {
                    previousState
                        .copy(
                            durationInProgress = false,
                            playlistVisible = false
                        )
                }
                is SongPartialState.LoadSongs.InFlight -> {
                    previousState
                        .copy(
                            durationInProgress = false,
                            playlistVisible = false
                        )
                }
                is SongPartialState.LoadSongs.Empty -> {
                    previousState
                        .copy(
                            replay = false,
                            shuffle = false,
                            playing = false,
                            durationInProgress = false,
                            playlistVisible = false,
                            data = null
                        )
                }
                is SongPartialState.LoadSongs.Loaded -> {
                    previousState
                        .copy(
                            replay = change.media.track.replay,
                            shuffle = change.media.track.shuffle,
                            playing = change.media.track.status == SongPlayer.PlayerStatus.PLAYING,
                            durationInProgress = false,
                            playlistVisible = false,
                            data = change.media
                        )
                }
                is SongPartialState.LoadSongs.Error -> {
                    previousState
                        .copy(
                            replay = false,
                            shuffle = false,
                            playing = false,
                            durationInProgress = false,
                            playlistVisible = false,
                            data = null,
                            error = change.throwable
                        )
                }
                is SongPartialState.SongPlayed -> {
                    previousState
                        .copy(
                            durationInProgress = false,
                            playlistVisible = false
                        )
                }
                is SongPartialState.SongResumed -> {
                    previousState
                        .copy(
                            durationInProgress = false,
                            playlistVisible = false,
                            playing = change.status == SongPlayer.PlayerStatus.PLAYING
                        )
                }
                is SongPartialState.TrackLoaded -> {
                    previousState
                        .copy(
                            durationInProgress = false,
                            playlistVisible = false,
                            playing = change.track.status == SongPlayer.PlayerStatus.PLAYING,
                            data = previousState.data
                                ?.copy(track = change.track)
                        )
                }
                is SongPartialState.DurationLoaded -> {
                    previousState
                        .copy(
                            durationInProgress = true,
                            playlistVisible = false,
                            data = previousState.data
                                ?.copy(
                                    track = previousState.data.track
                                        .copy(
                                            currentDuration = change.currentDuration,
                                            progress = change.progress
                                        )
                                )
                        )
                }
            }
        }
}

class PlayerViewModelFactory @Inject constructor(
    private val mInteractor: PlayerInteractor
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(PlayerInteractor::class.java)
            .newInstance(mInteractor)
    }
}