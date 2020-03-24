package com.bonioctavianus.android.themp3player.di

import android.content.ContentResolver
import android.media.MediaPlayer
import com.bonioctavianus.android.themp3player.Mp3App
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object Mp3Module {

    @JvmStatic
    @Singleton
    @Provides
    fun provideMediaPlayer(): MediaPlayer {
        return MediaPlayer()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideContentResolver(application: Mp3App): ContentResolver {
        return application.contentResolver
    }
}
