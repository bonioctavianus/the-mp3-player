package com.bonioctavianus.android.themp3player.base

import androidx.fragment.app.Fragment
import com.bonioctavianus.android.themp3player.utils.PermissionGranted
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

abstract class BaseFragment<I : Mp3Intent, S : Mp3ViewState> : Fragment(), Mp3View<I, S> {

    private val mEventBus = EventBus.getDefault()
    private var mIsRunning: Boolean = false

    override fun onStart() {
        super.onStart()
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this)
        }
    }

    @Subscribe
    fun onEvent(event: PermissionGranted) {
        if (!mIsRunning) {
            // this is called from Activity onResume due to Permission Checking
            // should only populate intent once
            bindIntent(intents())
            observeState()
            mIsRunning = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this)
        }
    }
}
