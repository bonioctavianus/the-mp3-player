package com.bonioctavianus.android.themp3player.usecase

import android.content.ContentUris
import android.media.MediaPlayer
import com.bonioctavianus.android.themp3player.Mp3App
import com.bonioctavianus.android.themp3player.model.Song
import com.bonioctavianus.android.themp3player.model.Track
import com.bonioctavianus.android.themp3player.ui.SongIntent
import com.bonioctavianus.android.themp3player.utils.TimeUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongPlayer @Inject constructor(
    private val mApplication: Mp3App,
    private val mMediaPlayer: MediaPlayer
) : MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    var mSongs: List<Song> = emptyList()
    private var mCurrentSong: Song? = null
    private var mForcePlay: Boolean = false
    private var mShuffle: Boolean = false
    private var mStatus: PlayerStatus = PlayerStatus.IDLE
    private val mDisposables = CompositeDisposable()
    private val mSubject: PublishSubject<SongIntent> = PublishSubject.create()

    init {
        mMediaPlayer.setOnPreparedListener(this)
    }

    fun prepare(song: Song, forcePlay: Boolean) {
        mCurrentSong = song
        mForcePlay = forcePlay

        val uri = ContentUris.withAppendedId(
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id
        )

        // sometimes after calling mediaPlayer.reset() when initializing song
        // onCompletion callback is called
        // and mediaPlayer playing "next" song in the list instead of the selected song
        mMediaPlayer.setOnCompletionListener(null)
        mMediaPlayer.reset()
        mMediaPlayer.setDataSource(mApplication, uri)
        mMediaPlayer.setOnPreparedListener(this)
        mMediaPlayer.prepareAsync()
    }

    fun setReplay(): Observable<Boolean> {
        return Observable.fromCallable {
            if (mSongs.isEmpty()) {
                return@fromCallable false
            }
            mMediaPlayer.isLooping = !mMediaPlayer.isLooping
            mMediaPlayer.isLooping
        }
    }

    fun setShuffle(status: Boolean): Observable<Boolean> {
        return Observable.fromCallable {
            if (mSongs.isEmpty()) {
                return@fromCallable false
            }
            if (mMediaPlayer.isLooping) {
                mMediaPlayer.isLooping = false
            }
            mShuffle = status.not()
            mShuffle
        }
    }

    fun setResume(): Observable<PlayerStatus> {
        return Observable.fromCallable {
            mStatus = if (mMediaPlayer.isPlaying) {
                mMediaPlayer.pause()
                mDisposables.clear()
                PlayerStatus.PAUSED
            } else {
                mMediaPlayer.start()
                startSongDurationEvent()
                PlayerStatus.PLAYING
            }
            mStatus
        }
            .onErrorReturnItem(PlayerStatus.IDLE)
    }

    fun setPosition(progress: Int): Observable<TaskResult> {
        return Observable.fromCallable {
            val position = TimeUtils.progressToTimer(progress, mMediaPlayer.duration)
            mMediaPlayer.seekTo(position)
            TaskResult.Success(progress)
        }
    }

    fun rewindPosition(time: Int): Observable<TaskResult> {
        return Observable.fromCallable {
            val currentPosition = mMediaPlayer.currentPosition

            if (currentPosition - time >= 0) {
                val newPosition = currentPosition - time
                mMediaPlayer.seekTo(newPosition)
                TaskResult.Success(
                    getProgressFromPosition(newPosition)
                )
            } else {
                mMediaPlayer.seekTo(0)
                TaskResult.Success(
                    getProgressFromPosition(0)
                )
            }
        }
    }

    fun forwardPosition(time: Int): Observable<TaskResult> {
        return Observable.fromCallable {
            val currentPosition = mMediaPlayer.currentPosition
            val totalDuration = mMediaPlayer.duration

            if (currentPosition + time <= totalDuration) {
                val newPosition = currentPosition + time
                mMediaPlayer.seekTo(newPosition)
                TaskResult.Success(
                    getProgressFromPosition(newPosition)
                )
            } else {
                mMediaPlayer.seekTo(totalDuration)
                TaskResult.Success(
                    getProgressFromPosition(totalDuration)
                )
            }
        }
    }

    private fun getProgressFromPosition(currentPosition: Int): Int {
        val totalDuration = mMediaPlayer.duration.toLong()
        return TimeUtils.getProgressPercentage(currentPosition.toLong(), totalDuration)
    }

    fun getActiveSong(): Observable<TaskResult> {
        val currentDuration = mMediaPlayer.currentPosition.toLong()
        val totalDuration = mMediaPlayer.duration.toLong()

        return Observable.just(
            TaskResult.Success(
                Track(
                    song = mCurrentSong ?: Song.default(),
                    replay = mMediaPlayer.isLooping,
                    shuffle = mShuffle,
                    status = mStatus,
                    totalDuration = TimeUtils.milliSecondsToTimer(totalDuration),
                    currentDuration = TimeUtils.milliSecondsToTimer(currentDuration),
                    progress = TimeUtils.getProgressPercentage(currentDuration, totalDuration)
                )
            )
        )
    }

    fun intents(): Observable<SongIntent> = mSubject.hide()

    private fun playSong() {
        mMediaPlayer.start()
        startSongDurationEvent()
        mStatus = PlayerStatus.PLAYING
        mMediaPlayer.setOnCompletionListener(this)
    }

    // a bit hack.. to play next song when UI is destroyed
    // only check for shuffle
    // because replay will not called onCompletion
    private fun playNextSong() {
        if (mSongs.isEmpty() || mCurrentSong == null) {
            return
        }

        if (mShuffle) {
            val randomIndex = (mSongs.indices).random()
            prepare(mSongs[randomIndex], true)
            return
        }

        val currentSongIndex = mSongs.indexOfFirst { it == mCurrentSong }
        if (currentSongIndex == -1) {
            return
        }

        if (currentSongIndex < mSongs.size - 1) {
            prepare(mSongs[currentSongIndex + 1], true)
        } else {
            prepare(mSongs.first(), true)
        }
    }

    private fun startSongDurationEvent() {
        mDisposables.add(
            Observable.interval(500, TimeUnit.MILLISECONDS)
                .map { SongIntent.GetTrackDuration }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        mSubject.onNext(
                            SongIntent.GetTrackDuration
                        )
                    },
                    { }
                )
        )
    }

    override fun onPrepared(mp: MediaPlayer?) {
        // immediately play selected song if user press Play button
        // or selected a song from Playlist
        if (mForcePlay) {
            playSong()

        } else {
            // for Previous/Next, only play the song if media player is currently playing
            if (mStatus == PlayerStatus.PLAYING) {
                playSong()
            }
        }

        mSubject.onNext(
            SongIntent.GetTrackInfo
        )
    }

    override fun onCompletion(mp: MediaPlayer?) {
        playNextSong()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Timber.e("Exception $what")
        return true
    }

    fun onCleared() {
        mMediaPlayer.release()
        mDisposables.dispose()
    }

    enum class PlayerStatus {
        IDLE,
        PLAYING,
        PAUSED
    }
}
