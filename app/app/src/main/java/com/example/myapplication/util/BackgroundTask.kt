package com.example.myapplication.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object BackgroundTask {
    private val executor: ExecutorService = Executors.newCachedThreadPool()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun <T> run(
        task: () -> T,
        onSuccess: (T) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        executor.execute {
            runCatching(task)
                .onSuccess { result -> mainHandler.post { onSuccess(result) } }
                .onFailure { error -> mainHandler.post { onFailure(error) } }
        }
    }
}
