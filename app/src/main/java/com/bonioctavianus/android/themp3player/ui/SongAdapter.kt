package com.bonioctavianus.android.themp3player.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bonioctavianus.android.themp3player.R
import com.bonioctavianus.android.themp3player.model.Song
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.row_item_song.view.*

class SongAdapter : RecyclerView.Adapter<SongViewHolder>() {

    private var mSongs: List<Song> = emptyList()
    private var mActiveSong: Song? = null
    private val mSubject: PublishSubject<SongIntent> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun getItemCount(): Int = mSongs.size

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(mSongs[position], mActiveSong).subscribe(mSubject)
    }

    fun getViewIntent(): Observable<SongIntent> = mSubject.hide()

    fun submitList(songs: List<Song>, activeSong: Song) {
        mSongs = songs
        mActiveSong = activeSong
        notifyDataSetChanged()
    }
}

class SongViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    fun bind(song: Song, activeSong: Song?): Observable<SongIntent> {
        with(song) {
            view.text_title.text = title
            view.text_artist.text = if (artist.isNotBlank()) artist else "Unknown Artist"

            view.image_art.setImageResource(
                if (song == activeSong) {
                    R.drawable.ic_play_arrow_focus
                } else {
                    R.drawable.ic_music_note
                }
            )

            view.text_title.setTextColor(
                if (song == activeSong) {
                    ContextCompat.getColor(view.context, R.color.colorAccent)
                } else {
                    ContextCompat.getColor(view.context, android.R.color.white)
                }
            )

            view.text_artist.setTextColor(
                if (song == activeSong) {
                    ContextCompat.getColor(view.context, R.color.colorAccent)
                } else {
                    ContextCompat.getColor(view.context, android.R.color.white)
                }
            )
        }
        return view.container_item.clicks()
            .map { SongIntent.SelectSong(song) }
    }
}
