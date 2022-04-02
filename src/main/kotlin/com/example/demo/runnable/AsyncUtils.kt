package com.example.demo.runnable

import java.util.concurrent.CompletionStage

import java.util.concurrent.ExecutionException

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.function.Supplier

class AsyncUtils {
    private val DEFAULT_TIMEOUT_SECONDS: Long = 5

    fun <T> awaitResult(completionStage: CompletionStage<T>, timeoutSeconds: Long): T {
        return try {
            completionStage.toCompletableFuture().get(timeoutSeconds, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            throw AssertionError(e)
        } catch (e: TimeoutException) {
            throw AssertionError(e)
        } catch (e: ExecutionException) {
            throw RuntimeExecutionException(e.cause)
        }
    }

    fun <T> awaitResult(completionStage: CompletionStage<T>): T {
        return awaitResult(completionStage, DEFAULT_TIMEOUT_SECONDS)
    }

    fun <T> awaitResult(completionStageSupplier: Supplier<CompletionStage<T>>, timeoutSeconds: Long): T {
        return awaitResult(completionStageSupplier.get(), timeoutSeconds)
    }

    fun <T> awaitResult(completionStageSupplier: Supplier<CompletionStage<T>>): T {
        return awaitResult(completionStageSupplier, DEFAULT_TIMEOUT_SECONDS)
    }

    private class RuntimeExecutionException internal constructor(cause: Throwable?) : RuntimeException(cause)
}