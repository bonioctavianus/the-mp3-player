package com.bonioctavianus.android.themp3player.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import com.bonioctavianus.android.themp3player.R
import com.bonioctavianus.android.themp3player.base.BaseFragment
import com.bonioctavianus.android.themp3player.usecase.SongPlayer
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_player.*
import timber.log.Timber
import javax.inject.Inject

class PlayerFragment : BaseFragment<SongIntent, SongViewState>() {

    @Inject
    lateinit var mViewModel: PlayerViewModel
    @Inject
    lateinit var mSongPlayer: SongPlayer

    private val mSubject: PublishSubject<SongIntent> = PublishSubject.create()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        player_view.mFragmentManager = activity?.supportFragmentManager
        player_view.mSongPlayer = mSongPlayer
        super.onViewCreated(view, savedInstanceState)
    }

    override fun intents(): Observable<SongIntent> {
        return Observable.merge(
            player_view.getViewIntent(),
            mSubject.hide()
        )
    }

    override fun bindIntent(intent: Observable<SongIntent>) {
        mViewModel.bindIntent(intent)
    }

    override fun observeState() {
        mViewModel.mState.observe(viewLifecycleOwner, Observer { value ->
            Timber.d("Received Value: $value")
            player_view.render(value)
        })

        mViewModel.mDurationState.observe(viewLifecycleOwner, Observer { value ->
            player_view.renderDuration(value)
            Timber.d("Received Duration: ${value.data?.track?.currentDuration}")
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_player_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_menu_playlist) {
            mSubject.onNext(
                SongIntent.ShowPlaylist
            )
        }
        return super.onOptionsItemSelected(item)
    }
}
