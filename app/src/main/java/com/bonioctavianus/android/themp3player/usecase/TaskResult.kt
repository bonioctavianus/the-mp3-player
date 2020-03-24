package com.bonioctavianus.android.themp3player.usecase

sealed class TaskResult {
    object InFlight : TaskResult()
    data class Success<T>(val item: T) : TaskResult()
    data class Error(val throwable: Throwable) : TaskResult()
}