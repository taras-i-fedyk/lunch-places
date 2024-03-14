package com.tarasfedyk.lunchplaces.biz.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A coroutine whose multiple instances cannot run simultaneously.
 * Calling [replaceableLaunch] cancels the currently running instance of the coroutine
 * and launches its new instance right afterward.
 *
 * @property scope the scope in which to launch an instance of this coroutine.
 * @property block the block of code to execute within the running instance of this coroutine.
 */
class ReplaceableLaunchCoroutine(
    private val scope: CoroutineScope,
    private val block: suspend () -> Unit
) {
    private val mutex = Mutex()
    private var job: Job? = null

    fun replaceableLaunch() {
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