package com.bonioctavianus.android.themp3player.ui

import com.bonioctavianus.android.themp3player.model.Media
import com.bonioctavianus.android.themp3player.model.Song
import com.bonioctavianus.android.themp3player.model.Track
import com.bonioctavianus.android.themp3player.service.ServiceRunner
import com.bonioctavianus.android.themp3player.usecase.SongManager
import com.bonioctavianus.android.themp3player.usecase.SongPlayer
import com.bonioctavianus.android.themp3player.usecase.TaskResult
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class PlayerInteractor @Inject constructor(
    private val mSongManager: SongManager,
    private val mSongPlayer: SongPlayer,
    private val mServiceRunner: ServiceRunner
) {

    fun compose(): ObservableTransformer<SongIntent, SongPartialState> {
        return ObservableTransformer { intents ->
            intents.publish { shared ->
                Observable.mergeArray(
                    shared.ofType(SongIntent.LoadSongs::class.java)
                        .compose(loadSongs),
                    shared.ofType(SongIntent.ShowPlaylist::class.java)
                        .compose(showPlaylist),
                    shared.ofType(SongIntent.ToggleReplay::class.java)
                        .compose(toggleReplay),
                    shared.ofType(SongIntent.ToggleShuffle::class.java)
                        .compose(toggleShuffle),
                    shared.ofType(SongIntent.TogglePlay::class.java)
                        .compose(togglePlay),
                    shared.ofType(SongIntent.SetPosition::class.java)
                        .compose(setPosition),
                    shared.ofType(SongIntent.RewindPosition::class.java)
                        .compose(rewindPosition),
                    shared.ofType(SongIntent.ForwardPosition::class.java)
                        .compose(forwardPosition),
                    shared.ofType(SongIntent.SwitchSong::class.java)
                        .compose(switchSong),
                    shared.ofType(SongIntent.SelectSong::class.java)
                        .compose(selectSong),
                    shared.ofType(SongIntent.GetTrackInfo::class.java)
                        .compose(getTrackInfo),
                    shared.ofType(SongIntent.GetTrackDuration::class.java)
                        .compose(getTrackDuration)
                )
            }
        }
    }

    private val loadSongs =
        ObservableTransformer<SongIntent.LoadSongs, SongPartialState> { intents ->
            intents.flatMap {
                Observable.combineLatest(
                    mSongManager.getSongs(),
                    mSongPlayer.getActiveSong()
                        .cast(TaskResult.Success::class.java)
                        .map { it.item as Track },
                    BiFunction { t1: TaskResult, t2: Track ->
                        if (t1 is TaskResult.Success<*>) {
                            val songs = t1.item as List<Song>
                            val track = getCurrentTrack(songs, t2)
                            // terrible hack.. too lazy to refactor
                            mSongPlayer.mSongs = songs
                            return@BiFunction TaskResult.Success(
                                Media(songs = songs, track = track)
                            )
                        }
                        t1
                    }
                )
                    .map(::mapGetMedia)
            }
        }

    private fun getCurrentTrack(songs: List<Song>, track: Track): Track {
        return when {
            track.song.id > 0 -> track
            songs.isNotEmpty() -> track.copy(song = songs[0])
            else -> track.copy(song = Song.default())
        }
    }

    private fun mapGetMedia(result: TaskResult): SongPartialState {
        return when (result) {
            is TaskResult.InFlight -> {
                SongPartialState.LoadSongs.InFlight
            }
            is TaskResult.Success<*> -> {
                val player = result.item as Media

                if (player.songs.isEmpty()) {
                    SongPartialState.LoadSongs.Empty
                } else {
                    SongPartialState.LoadSongs.Loaded(
                        player
                    )
                }
            }
            is TaskResult.Error -> {
                SongPartialState.LoadSongs.Error(
                    result.throwable
                )
            }
        }
    }

    private val showPlaylist =
        ObservableTransformer<SongIntent.ShowPlaylist, SongPartialState> { intents ->
            intents.flatMap {
                Observable.just(
                    SongPartialState.PlaylistVisible
                )
            }
        }

    private val toggleReplay =
        ObservableTransformer<SongIntent.ToggleReplay, SongPartialState> { intents ->
            intents.flatMap {
                mSongPlayer.setReplay()
                    .map {
                        SongPartialState.ReplayChanged(
                            it
                        )
                    }
            }
        }

    private val toggleShuffle =
        ObservableTransformer<SongIntent.ToggleShuffle, SongPartialState> { intents ->
            intents.flatMap { intent ->
                mSongPlayer.setShuffle(intent.status)
                    .map {
                        SongPartialState.ShuffleChanged(
                            it
                        )
                    }
            }
        }

    private val togglePlay =
        ObservableTransformer<SongIntent.TogglePlay, SongPartialState> { intents ->
            intents.flatMap { intent ->
                mSongPlayer.getActiveSong()
                    .cast(TaskResult.Success::class.java)
                    .map { it.item as Track }
                    .flatMap check@{ track ->
                        if (track.status == SongPlayer.PlayerStatus.IDLE) {
                            return@check Observable.fromCallable {
                                mServiceRunner.startSongService(intent.song, forcePlay = true)
                                SongPartialState.SongPlayed
                            }
                        } else {
                            return@check mSongPlayer.setResume()
                                .map {
                                    SongPartialState.SongResumed(
                                        it
                                    )
                                }
                        }
                    }
            }
        }

    private val setPosition =
        ObservableTransformer<SongIntent.SetPosition, SongPartialState> { intents ->
            intents.flatMap { intent ->
                mSongPlayer.setPosition(intent.progress)
                    .map { SongPartialState.PositionChanged }
            }
        }

    private val rewindPosition =
        ObservableTransformer<SongIntent.RewindPosition, SongPartialState> { intents ->
            intents.flatMap { intent ->
                mSongPlayer.rewindPosition(intent.time)
                    .map { SongPartialState.PositionChanged }
            }
        }

    private val forwardPosition =
        ObservableTransformer<SongIntent.ForwardPosition, SongPartialState> { intents ->
            intents.flatMap { intent ->
                mSongPlayer.forwardPosition(intent.time)
                    .map { SongPartialState.PositionChanged }
            }
        }

    private val switchSong =
        ObservableTransformer<SongIntent.SwitchSong, SongPartialState> { intents ->
            intents.flatMap { intent ->
                Observable.fromCallable {
                    mServiceRunner.startSongService(intent.song)
                    SongPartialState.SongPlayed
                }
            }
        }

    private val selectSong =
        ObservableTransformer<SongIntent.SelectSong, SongPartialState> { intents ->
            intents.flatMap { intent ->
                Observable.fromCallable {
                    mServiceRunner.startSongService(intent.song, forcePlay = true)
                    SongPartialState.SongPlayed
                }
            }
        }

    private val getTrackInfo =
        ObservableTransformer<SongIntent.GetTrackInfo, SongPartialState> { intents ->
            intents.flatMap {
                mSongPlayer.getActiveSong()
                    .cast(TaskResult.Success::class.java)
                    .map { it.item as Track }
                    .map {
                        SongPartialState.TrackLoaded(
                            it
                        )
                    }
            }
        }

    private val getTrackDuration =
        ObservableTransformer<SongIntent.GetTrackDuration, SongPartialState> { intents ->
            intents.flatMap {
                mSongPlayer.getActiveSong()
                    .cast(TaskResult.Success::class.java)
                    .map { it.item as Track }
                    .map {
                        SongPartialState.DurationLoaded(
                            currentDuration = it.currentDuration,
                            progress = it.progress
                        )
                    }
            }
        }
}
