package com.bonioctavianus.android.themp3player

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.bonioctavianus.android.themp3player.di.DaggerMp3Component
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import javax.inject.Inject

class Mp3App : Application(), HasAndroidInjector {

    companion object {
        const val NOTIFICATION_CHANNEL_SONG = "1"
    }

    @Inject
    lateinit var mAndroidInjector: DispatchingAndroidInjector<Any>

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        initNotificationChannel()

        DaggerMp3Component.factory().create(this).inject(this)
    }

    private fun initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val songChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_SONG,
                "Song Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    enableLights(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(songChannel)
        }
    }

    override fun androidInjector(): AndroidInjector<Any> = mAndroidInjector
}
