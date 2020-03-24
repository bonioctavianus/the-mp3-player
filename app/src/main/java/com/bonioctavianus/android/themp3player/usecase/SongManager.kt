package com.bonioctavianus.android.themp3player.usecase

import android.content.ContentResolver
import android.database.Cursor
import com.bonioctavianus.android.themp3player.model.Song
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongManager @Inject constructor(
    private val mContentResolver: ContentResolver
) {

    fun getSongs(): Observable<TaskResult> {
        return Observable.fromCallable {
            TaskResult.Success(querySongs())
        }
            .cast(TaskResult::class.java)
            .startWith(TaskResult.InFlight)
            .onErrorReturn { throwable ->
                TaskResult.Error(throwable)
            }
    }

    private fun querySongs(): List<Song> {
        val uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor? = mContentResolver.query(uri, null, null, null, null)
        val songs = mutableListOf<Song>()

        when {
            cursor == null -> {
                // query failed, handle error.
            }
            !cursor.moveToFirst() -> {
                // no media on the device
            }
            else -> {
                val idColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
                val titleColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
                val artistColumn: Int =
                    cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)

                do {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn)

                    songs.add(
                        Song(id = id, title = title, artist = artist)
                    )

                } while (cursor.moveToNext())
            }
        }
        cursor?.close()

        return songs
    }
}
