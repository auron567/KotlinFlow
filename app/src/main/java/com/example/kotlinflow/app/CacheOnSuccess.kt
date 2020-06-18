package com.example.kotlinflow.app

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Cache the first non-error result from an async computation passed as [block].
 *
 * Usage:
 *
 * ```
 * val cachedSuccess: CacheOnSuccess<Int> = CacheOnSuccess(onErrorFallback = { 3 }) {
 *     delay(1_000)
 *     5
 * }
 *
 * cachedSucces.getOrAwait()
 * ```
 *
 * The result of [block] will be cached after the fist successful result, and future calls to
 * [getOrAwait] will return the cached value.
 *
 * If multiple coroutines call [getOrAwait] before [block] returns, then [block] will only execute
 * one time. If successful, they will all get the same success result.
 *
 * [onErrorFallback] will always be called (if it is not null) in case of error and will never cache
 * the error result.
 */
class CacheOnSuccess<T: Any>(
    private val onErrorFallback: (suspend () -> T)? = null,
    private val block: suspend () -> T
) {
    private val mutex = Mutex()

    @Volatile
    private var deferred: Deferred<T>? = null

    suspend fun getOrAwait(): T {
        return supervisorScope {
            // Only allow one coroutine to try running block at a time by using a Mutex
            val currentDeferred = mutex.withLock {
                deferred?.let { return@withLock it }

                async {
                    block()
                }.also {
                    deferred = it
                }
            }

            // Await the result, with our custom error handling
            currentDeferred.safeAway()
        }
    }

    private suspend fun Deferred<T>.safeAway(): T {
        try {
            // This call to await always throw exception if this coroutine is cancelled
            return await()
        } catch (throwable: Throwable) {
            // Clear deferred because we don't want to cache errors
            mutex.withLock {
                if (deferred == this) {
                    deferred = null
                }
            }

            // Never consume cancellation
            if (throwable is CancellationException) {
                throw throwable
            }

            // Return fallback if provided
            onErrorFallback?.let { fallback -> return fallback() }

            // If fallback is not provided throw exception
            throw throwable
        }
    }
}