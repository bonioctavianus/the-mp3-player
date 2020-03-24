package com.bonioctavianus.android.themp3player.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.fragment.app.FragmentManager
import com.bonioctavianus.android.themp3player.R
import com.bonioctavianus.android.themp3player.usecase.SongPlayer
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.widget_player.view.*

class PlayerView(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet), SeekBar.OnSeekBarChangeListener {

    companion object {
        private const val FORWARD_REWIND_TIME = 5000
        private const val SEEKBAR_MAX_VALUE = 100
    }

    var mFragmentManager: FragmentManager? = null
    var mSongPlayer: SongPlayer? = null

    private var mCurrentState: SongViewState? = null
    private val mPlaylistView = PlaylistFragment()
    private val mSubject: PublishSubject<SongIntent> = PublishSubject.create()

    init {
        View.inflate(context, R.layout.widget_player, this)

        // to make the marquee text working
        text_title.isSelected = true
        seekbar.max = SEEKBAR_MAX_VALUE
        seekbar.setOnSeekBarChangeListener(this)
    }

    fun render(state: SongViewState) {
        mCurrentState = state

        renderReplay()
        renderShuffle()
        renderPlaying()
        renderPlaylist()
        renderData()
    }

    private fun renderReplay() {
        if (mCurrentState?.replay == true) {
            image_replay.setImageResource(R.drawable.button_replay_active)
        } else {
            image_replay.setImageResource(R.drawable.button_replay)
        }
    }

    private fun renderShuffle() {
        if (mCurrentState?.shuffle == true) {
            image_shuffle.setImageResource(R.drawable.button_shuffle_active)
        } else {
            image_shuffle.setImageResource(R.drawable.button_shuffle)
        }
    }

    private fun renderPlaying() {
        if (mCurrentState?.playing == true) {
            image_play.setImageResource(R.drawable.button_pause)
        } else {
            image_play.setImageResource(R.drawable.button_play)
        }
    }

    private fun renderPlaylist() {
        if (mCurrentState?.playlistVisible == true) {
            mFragmentManager?.let {
                if (!mPlaylistView.isVisible) {
                    mPlaylistView.show(it, "Show Song Playlist")
                }
            }
        } else {
            mFragmentManager?.let {
                if (mPlaylistView.isVisible) {
                    mPlaylistView.dismiss()
                }
            }
        }
    }

    private fun renderData() {
        val media = mCurrentState?.data
        if (media == null) {
            text_title.setText(R.string.player_no_songs_title)
            text_artist.setText(R.string.player_no_songs_description)
            return
        }

        val track = media.track
        with(track) {
            if (song.title != text_title.text) {
                text_title.text = song.title
            }
            if (song.artist != text_artist.text) {
                text_artist.text = if (song.artist.isNotBlank()) song.artist else "Unknown Artist"
            }

            text_current_duration.text = currentDuration
            text_total_duration.text = totalDuration
        }
        mPlaylistView.render(media.songs, track.song)
    }

    fun renderDuration(state: SongViewState) {
        val currentDuration = state.data?.track?.currentDuration ?: "0:00"
        val progress = state.data?.track?.progress ?: 0
        text_current_duration.text = currentDuration
        seekbar.progress = progress
    }

    fun getViewIntent(): Observable<SongIntent> {
        return Observable.mergeArray(
            getLoadSongsIntent(),
            getButtonReplayIntent(),
            getButtonShuffleIntent(),
            getButtonPlayIntent(),
            getButtonRewindIntent(),
            getButtonForwardIntent(),
            getButtonPreviousIntent(),
            getButtonNextIntent(),
            mPlaylistView.getViewIntent(),
            mSongPlayer?.intents(),
            mSubject.hide()
        )
    }

    private fun getLoadSongsIntent(): Observable<SongIntent> {
        return Observable.just(
            SongIntent.LoadSongs
        )
    }

    private fun getButtonReplayIntent(): Observable<SongIntent> {
        return image_replay.clicks()
            .map { SongIntent.ToggleReplay }
    }

    private fun getButtonShuffleIntent(): Observable<SongIntent> {
        return image_shuffle.clicks()
            .map {
                SongIntent.ToggleShuffle(
                    mCurrentState?.shuffle ?: false
                )
            }
    }

    private fun getButtonPlayIntent(): Observable<SongIntent> {
        return Observable.create { emitter ->
            image_play.setOnClickListener {
                val song = mCurrentState?.data?.track?.song ?: return@setOnClickListener
                emitter.onNext(
                    SongIntent.TogglePlay(song)
                )
            }
        }
    }

    private fun getButtonRewindIntent(): Observable<SongIntent> {
        return image_rewind.clicks()
            .map { SongIntent.RewindPosition(FORWARD_REWIND_TIME) }
    }

    private fun getButtonForwardIntent(): Observable<SongIntent> {
        return image_forward.clicks()
            .map { SongIntent.ForwardPosition(FORWARD_REWIND_TIME) }
    }

    private fun getButtonPreviousIntent(): Observable<SongIntent> {
        return Observable.create { emitter ->
            image_previous.setOnClickListener {
                val songs = mCurrentState?.data?.songs
                val song = mCurrentState?.data?.track?.song

                if (songs.isNullOrEmpty()) {
                    return@setOnClickListener
                }
                if (song?.isDefault() == true) {
                    return@setOnClickListener
                }

                val currentSongIndex = songs.indexOfFirst { it == song }
                if (currentSongIndex == -1) {
                    return@setOnClickListener
                }

                if (currentSongIndex > 0) {
                    emitter.onNext(
                        SongIntent.SwitchSong(songs[currentSongIndex - 1])
                    )
                } else {
                    emitter.onNext(
                        SongIntent.SwitchSong(songs.last())
                    )
                }
            }
        }
    }

    private fun getButtonNextIntent(): Observable<SongIntent> {
        return Observable.create { emitter ->
            image_next.setOnClickListener {
                val songs = mCurrentState?.data?.songs
                val song = mCurrentState?.data?.track?.song

                if (songs.isNullOrEmpty()) {
                    return@setOnClickListener
                }
                if (song?.isDefault() == true) {
                    return@setOnClickListener
                }

                val currentSongIndex = songs.indexOfFirst { it == song }
                if (currentSongIndex == -1) {
                    return@setOnClickListener
                }

                if (currentSongIndex < songs.size - 1) {
                    emitter.onNext(
                        SongIntent.SwitchSong(songs[currentSongIndex + 1])
                    )
                } else {
                    emitter.onNext(
                        SongIntent.SwitchSong(songs.first())
                    )
                }
            }
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = Unit

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        mSubject.onNext(
            SongIntent.SetPosition(seekBar?.progress ?: 0)
        )
    }
}
