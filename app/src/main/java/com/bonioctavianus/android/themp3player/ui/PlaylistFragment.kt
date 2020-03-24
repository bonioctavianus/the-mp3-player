package com.bonioctavianus.android.themp3player.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bonioctavianus.android.themp3player.R
import com.bonioctavianus.android.themp3player.model.Song
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.Observable
import kotlinx.android.synthetic.main.widget_playlist.view.*

class PlaylistFragment : BottomSheetDialogFragment() {

    private val mAdapter = SongAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.widget_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.list_songs.layoutManager = LinearLayoutManager(context)
        view.list_songs.adapter = mAdapter
    }

    fun render(songs: List<Song>, activeSong: Song) {
        mAdapter.submitList(songs, activeSong)
    }

    fun getViewIntent(): Observable<SongIntent> {
        return mAdapter.getViewIntent()
    }
}
