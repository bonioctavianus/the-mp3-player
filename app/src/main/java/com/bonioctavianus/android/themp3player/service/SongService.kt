package com.bonioctavianus.android.themp3player.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bonioctavianus.android.themp3player.ui.MainActivity
import com.bonioctavianus.android.themp3player.Mp3App
import com.bonioctavianus.android.themp3player.R
import com.bonioctavianus.android.themp3player.model.Song
import com.bonioctavianus.android.themp3player.model.Track
import com.bonioctavianus.android.themp3player.ui.SongIntent
import com.bonioctavianus.android.themp3player.usecase.SongPlayer
import com.bonioctavianus.android.themp3player.usecase.TaskResult
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SongService : Service() {

    companion object {
        private const val SERVICE_ID = 1
    }

    @Inject
    lateinit var mSongPlayer: SongPlayer

    private val mDisposables = CompositeDisposable()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val song = intent?.getParcelableExtra<Song>(ServiceRunner.KEY_SONG)
        song ?: throw NullPointerException("Song is null")
        val forcePlay = intent.getBooleanExtra(ServiceRunner.KEY_FORCE_PLAY, false)

        startForeground(SERVICE_ID, createNotification(song))

        mSongPlayer.prepare(song, forcePlay)

        mDisposables.add(
            mSongPlayer.intents()
                .filter { it is SongIntent.GetTrackInfo }
                .flatMap { mSongPlayer.getActiveSong() }
                .cast(TaskResult.Success::class.java)
                .map { it.item as Track }
                .subscribe(
                    { track ->
                        NotificationManagerCompat.from(this)
                            .notify(SERVICE_ID, createNotification(track.song))
                    },
                    { }
                )
        )

        return START_STICKY
    }

    private fun createNotification(song: Song): Notification {
        val builder = NotificationCompat.Builder(this, Mp3App.NOTIFICATION_CHANNEL_SONG)
            .setSmallIcon(R.drawable.ic_play_circle_normal)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(
                createContentPendingIntent()
            )
            .setOngoing(true)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        return builder.build()
    }

    private fun createContentPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
            .apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
        return PendingIntent.getActivity(
            this,
            SERVICE_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mSongPlayer.onCleared()
        mDisposables.dispose()
    }
}
