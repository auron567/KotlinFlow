package com.example.kotlinflow.app

import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.*
import org.junit.Assert.assertEquals
import org.junit.Test

class CacheOnSuccessTest {

    @Test
    fun getOrAwait_cachesResult() = runBlocking {
        var counter = 0

        val cachedValue = CacheOnSuccess {
            counter++
        }

        cachedValue.getOrAwait()
        assertEquals(0, cachedValue.getOrAwait())
        assertEquals(1, counter)
    }

    @Test
    fun getOrAwait_cachesResultWithMultipleAwaits() = runBlocking {
        val completable = CompletableDeferred<Unit>()
        var counter = 0

        val cachedValue = CacheOnSuccess {
            completable.await()
            counter++
        }

        val first = async { cachedValue.getOrAwait() }
        val second = async { cachedValue.getOrAwait() }
        completable.complete(Unit)

        assertEquals(0, first.await())
        assertEquals(0, second.await())
        assertEquals(1, counter)
    }

    @Test
    fun getOrAwait_throwsException() = runBlocking {
        val cachedValue = CacheOnSuccess<Int> {
            throw SomeException()
        }

        val result = runCatching { cachedValue.getOrAwait() }
        assertTrue(result.exceptionOrNull() is SomeException)
    }

    @Test
    fun getOrAwait_doesntCachesException() = runBlocking {
        var counter = 0

        val cachedValue = CacheOnSuccess {
            if (counter++ == 0) {
                throw SomeException()
            } else {
                counter
            }
        }

        runCatching { cachedValue.getOrAwait() }
        assertEquals(2, cachedValue.getOrAwait())
    }

    @Test
    fun getOrAwait_propagatesCancellation() = runBlocking {
        val cachedValue = CacheOnSuccess {
            withTimeout(50L) {
                delay(100L)
                1
            }
        }

        val result = runCatching { cachedValue.getOrAwait() }
        assertTrue(result.exceptionOrNull() is CancellationException)
    }

    @Test
    fun getOrAwait_propagatesCancellationWithFallback() = runBlocking {
        val cachedValue = CacheOnSuccess(onErrorFallback = { 2 }) {
            withTimeout(50L) {
                delay(100L)
                1
            }
        }

        val result = kotlin.runCatching { cachedValue.getOrAwait() }
        assertTrue(result.exceptionOrNull() is CancellationException)
    }

    @Test
    fun getOrAwait_callsFallback() = runBlocking {
        var counter = 0

        val cachedValue = CacheOnSuccess(onErrorFallback = { counter++ }) {
            throw SomeException()
        }

        assertEquals(0, cachedValue.getOrAwait())
        assertEquals(1, cachedValue.getOrAwait())
        assertEquals(2, counter)
    }

    @Test
    fun getOrAwait_callsFallbackWithMultipleAwaits() = runBlocking {
        var counter = 0

        val cachedValue = CacheOnSuccess(onErrorFallback = { counter++ }) {
            throw SomeException()
        }

        launch { cachedValue.getOrAwait() }
        launch { cachedValue.getOrAwait() }
        launch { cachedValue.getOrAwait() }

        delay(50L)
        assertEquals(3, cachedValue.getOrAwait())
        assertEquals(4, counter)
    }
}

class SomeException : Throwable()