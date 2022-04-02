package com.example.demo.runnable

import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.vavr.API
import io.vavr.Predicates
import io.vavr.control.Try
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import javax.xml.ws.WebServiceException


@DisplayName("RunnableRetry Test ")
@ExtendWith(MockitoExtension::class)
class RunnableRetryTest {
    @Mock
    private val helloWorldService: HelloWorldService? = null
    @Test
    fun shouldNotRetry() {
        // Create a Retry with default configuration
        val retryContext = Retry.ofDefaults("id")

        // Decorate the invocation of the HelloWorldService
        val runnable = Retry.decorateRunnable(
            retryContext
        ) { helloWorldService!!.sayHelloWorld() }

        // When
        runnable.run()
        // Then the helloWorldService should be invoked 1 time
        BDDMockito.then(helloWorldService).should(Mockito.times(1))?.sayHelloWorld()
    }

    @Test
    fun testDecorateRunnable() {
        // Given the HelloWorldService throws an exception
        BDDMockito.willThrow(WebServiceException("BAM!")).given(helloWorldService)?.sayHelloWorld()

        // Create a Retry with default configuration
        val retry = Retry.ofDefaults("id")
        // Decorate the invocation of the HelloWorldService
        val runnable = Retry.decorateRunnable(
            retry
        ) { helloWorldService!!.sayHelloWorld() }

        // When
        val result = Try.run { runnable.run() }

        // Then the helloWorldService should be invoked 3 times
        BDDMockito.then(helloWorldService).should(Mockito.times(3))?.sayHelloWorld()
        // and the result should be a failure
        Assertions.assertTrue(result.isFailure)
        // and the returned exception should be of type RuntimeException
        Assertions.assertTrue(result.failed().get() is WebServiceException)
    }

    @Test
    fun testExecuteRunnable() {
        // Create a Retry with default configuration
        val retry = Retry.ofDefaults("id")
        // Decorate the invocation of the HelloWorldService
        retry.executeRunnable { helloWorldService!!.sayHelloWorld() }

        // Then the helloWorldService should be invoked 1 time
        BDDMockito.then(helloWorldService).should(Mockito.times(1))?.sayHelloWorld()
    }

    @Test
    fun shouldReturnAfterThreeAttempts() {
        // Given the HelloWorldService throws an exception
        BDDMockito.willThrow(WebServiceException("BAM!")).given(helloWorldService)?.sayHelloWorld()

        // Create a Retry with default configuration
        val retry = Retry.ofDefaults("id")
        // Decorate the invocation of the HelloWorldService
        val retryableRunnable = Retry.decorateCheckedRunnable(
            retry
        ) { helloWorldService!!.sayHelloWorld() }

        // When
        val result = Try.run(retryableRunnable)

        // Then the helloWorldService should be invoked 3 times
        BDDMockito.then(helloWorldService).should(Mockito.times(3))?.sayHelloWorld()
        // and the result should be a failure
        Assertions.assertTrue(result.isFailure)
        // and the returned exception should be of type RuntimeException
        Assertions.assertTrue(result.failed().get() is WebServiceException)
    }

    @Test
    fun shouldReturnAfterOneAttempt() {
        // Given the HelloWorldService throws an exception
        BDDMockito.willThrow(WebServiceException("BAM!")).given(helloWorldService)?.sayHelloWorld()

        // Create a Retry with default configuration
        val config = RetryConfig.custom<Any>().maxAttempts(1).build()
        val retry = Retry.of("id", config)
        // Decorate the invocation of the HelloWorldService
        val retryableRunnable = Retry.decorateCheckedRunnable(
            retry
        ) { helloWorldService!!.sayHelloWorld() }

        // When
        val result = Try.run(retryableRunnable)

        // Then the helloWorldService should be invoked 1 time
        BDDMockito.then(helloWorldService).should(Mockito.times(1))?.sayHelloWorld()
        // and the result should be a failure
        Assertions.assertTrue(result.isFailure)
        // and the returned exception should be of type RuntimeException
        Assertions.assertTrue(result.failed().get() is WebServiceException)
    }

    @Test
    fun shouldReturnAfterOneAttemptAndIgnoreException() {
        // Given the HelloWorldService throws an exception
        BDDMockito.willThrow(WebServiceException("BAM!")).given(helloWorldService)?.sayHelloWorld()

        // Create a Retry with default configuration
        val config = RetryConfig.custom<Any>()
            .retryOnException { throwable: Throwable ->
                API.Match(throwable).of(
                    API.Case(
                        API.`$`(
                            Predicates.instanceOf(
                                WebServiceException::class.java
                            )
                        ), false
                    ),
                    API.Case(API.`$`(), true)
                )
            }
            .build()
        val retry = Retry.of("id", config)

        // Decorate the invocation of the HelloWorldService
        val retryableRunnable = Retry.decorateCheckedRunnable(
            retry
        ) { helloWorldService!!.sayHelloWorld() }

        // When
        val result = Try.run(retryableRunnable)

        // Then the helloWorldService should be invoked only once, because the exception should be rethrown immediately.
        BDDMockito.then(helloWorldService).should(Mockito.times(1))?.sayHelloWorld()
        // and the result should be a failure
        Assertions.assertTrue(result.isFailure)
        // and the returned exception should be of type RuntimeException
        Assertions.assertTrue(result.failed().get() is WebServiceException)
    }
}