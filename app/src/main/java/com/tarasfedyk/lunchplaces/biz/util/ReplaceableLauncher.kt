package com.tarasfedyk.lunchplaces.biz.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Launches coroutines in such a way that
 * a newly launched coroutine gracefully replaces the previously launched one.
 *
 * @property scope the scope in which to launch coroutines
 */
class ReplaceableLauncher(
    private val scope: CoroutineScope
) {
    private val mutex = Mutex()
    private var job: Job? = null

    fun launch(block: suspend CoroutineScope.() -> Unit) {
        scope.launch {
            mutex.withLock {
                job?.cancelAndJoin()
                job = launch {
                    try {
                        block()
                    } finally {
                        job = null
                    }
                }
            }
        }
    }
}