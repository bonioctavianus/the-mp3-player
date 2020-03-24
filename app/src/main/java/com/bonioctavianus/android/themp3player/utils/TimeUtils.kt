package com.bonioctavianus.android.themp3player.utils

object TimeUtils {

    fun milliSecondsToTimer(milliseconds: Long): String {
        var finalTimerString = ""
        var secondsString = ""

        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()

        if (hours > 0) {
            finalTimerString = "$hours:"
        }

        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }

        return "$finalTimerString$minutes:$secondsString"
    }

    fun getProgressPercentage(currentDuration: Long, totalDuration: Long): Int {
        val currentSeconds: Long = (currentDuration / 1000)
        val totalSeconds: Long = (totalDuration / 1000)
        return (currentSeconds.toDouble() / totalSeconds * 100).toInt()
    }

    fun progressToTimer(progress: Int, total: Int): Int {
        val totalDuration = (total / 1000)
        return (progress.toDouble() / 100 * totalDuration).toInt() * 1000
    }
}