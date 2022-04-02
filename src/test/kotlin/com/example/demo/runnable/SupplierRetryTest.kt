package com.example.demo.runnable

import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer
import java.util.function.Supplier
import java.util.stream.Stream

class SupplierRetryTest {

    @Test
    fun createSupplierAndUseMonoAndThrowException() {
        val config = RetryConfig.custom<Any>()
            .maxAttempts(1)
            .build()

        val retry = Retry.of("default", config)

        val c = AtomicLong(0)
        val supplier: Supplier<String> = Supplier<String> {
            val count: Long = c.getAndAdd(1)
            if (count == 0L) {
                throw RuntimeException("err-$count")
            }
            "Result - $count"
        }

        val decoratedSupplier = Retry.decorateSupplier(retry, supplier)

        Mono.fromSupplier(decoratedSupplier).subscribe(System.out::println)
    }

    @Test
    fun createSupplierAndUseMonoAndNotThrownException() {
        val config = RetryConfig.custom<Any>()
            .maxAttempts(2)
            .build()

        val retry = Retry.of("default", config)

        val c = AtomicLong(0)
        val supplier: Supplier<String> = Supplier<String> {
            val count: Long = c.getAndAdd(1)
            if (count == 0L) {
                throw RuntimeException("err-$count")
            }
            "Result - $count"
        }

        val decoratedSupplier = Retry.decorateSupplier(retry, supplier)

        Mono.fromSupplier(decoratedSupplier).subscribe(System.out::println)
    }

    @Test
    fun createSupplierAndUseFluxAndThrownException() {
        val config = RetryConfig.custom<Any>()
            .maxAttempts(1)
            .build()

        val retry = Retry.of("default", config)

        val c = AtomicInteger(0)
        val supplier: Supplier<String> = Supplier<String> {
            val count = c.getAndAdd(1)
            if (count % 2 == 0) {
                throw RuntimeException("err-$count")
            }
            "Result - $count"
        }

        val decoratedSupplier = Retry.decorateSupplier(retry, supplier)

        val consumer = Consumer<String> { println(it) }

        Flux.fromStream(Stream.generate { decoratedSupplier })
            .map { it.get() }
            .subscribe(consumer)
    }

    @Test
    fun createSupplierAndUseFluxAndNotThrownException() {
        val config = RetryConfig.custom<Any>()
            .maxAttempts(2)
            .build()

        val retry = Retry.of("default", config)

        val c = AtomicInteger(0)
        val supplier: Supplier<String> = Supplier<String> {
            val count = c.getAndAdd(1)
            if (count % 2 == 0 || count == 10 || count == 11) {
                throw RuntimeException("err-$count")
            }
            "Result - $count"
        }

        val decoratedSupplier = Retry.decorateSupplier(retry, supplier)

        val consumer = Consumer<String> { println(it) }

        Flux.fromStream(Stream.generate { decoratedSupplier })
            .map { it.get() }
            .doOnNext(consumer)
            .subscribe()
    }
}