package com.bonioctavianus.android.themp3player.di

import com.bonioctavianus.android.themp3player.service.SongService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    abstract fun contributeSongService(): SongService
}