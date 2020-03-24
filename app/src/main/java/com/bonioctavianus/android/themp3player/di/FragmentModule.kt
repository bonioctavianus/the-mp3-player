package com.bonioctavianus.android.themp3player.di

import com.bonioctavianus.android.themp3player.ui.PlayerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentModule {

    @ContributesAndroidInjector(modules = [PlayerModule::class])
    abstract fun contributePlayerFragment(): PlayerFragment
}