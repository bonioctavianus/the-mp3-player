package com.bonioctavianus.android.themp3player.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.bonioctavianus.android.themp3player.ui.PlayerFragment
import com.bonioctavianus.android.themp3player.ui.PlayerViewModel
import com.bonioctavianus.android.themp3player.ui.PlayerViewModelFactory
import dagger.Module
import dagger.Provides

@Module
object PlayerModule {

    @Provides
    fun provideViewModelStoreOwner(fragment: PlayerFragment): ViewModelStoreOwner {
        return fragment
    }

    @Provides
    fun providePlayerViewModel(
        owner: ViewModelStoreOwner,
        factory: PlayerViewModelFactory
    ): PlayerViewModel {
        return ViewModelProvider(owner, factory).get(PlayerViewModel::class.java)
    }
}