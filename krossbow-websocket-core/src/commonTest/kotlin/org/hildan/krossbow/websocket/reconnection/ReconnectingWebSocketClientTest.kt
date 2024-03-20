package org.hildan.krossbow.websocket.reconnection

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.hildan.krossbow.websocket.*
import org.hildan.krossbow.websocket.test.*
import kotlin.coroutines.*
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class ReconnectingWebSocketClientTest {

    @Test
    fun shouldConnectSuccessfully() = runTest {
        val webSocketClientMock = ControlledWebSocketClientMock()
        val reconnectingClient = webSocketClientMock.withAutoReconnect { reconnectContext = testDispatcher }

        val deferredConnection = async { reconnectingClient.connect("dummy") }

        webSocketClientMock.waitForConnectCall()
        advanceUntilIdle()
        assertFalse(deferredConnection.isCompleted, "connect() call should suspend until base client connects")

        webSocketClientMock.simulateSuccessfulConnection(WebSocketConnectionMock())
        val connection = withTimeoutOrNull(50) { deferredConnection.await() }
        assertNotNull(connection, "connect() call should resume after base client connects")
    }

    @Test
    fun shouldFailConnectionWhenBaseClientFails() = runTest {
        val webSocketClientMock = ControlledWebSocketClientMock()
        val reconnectingClient = webSocketClientMock.withAutoReconnect { reconnectContext = testDispatcher }

        launch {
            webSocketClientMock.waitForConnectCall()
            webSocketClientMock.simulateFailedConnection(Exception("something bad"))
        }
        val exception = assertFailsWith(Exception::class) {
            reconnectingClient.connect("dummy")
        }
        assertEquals("something bad", exception.message)
    }

    @Test
    fun shouldForwardFrames() = runTest {
        val baseConnection = WebSocketConnectionMock()
        val baseClient = webSocketClientMock { baseConnection }

        val reconnectingClient = baseClient.withAutoReconnect { reconnectContext = testDispatcher }
        val connection = reconnectingClient.connect("dummy")

        launch { baseConnection.simulateTextFrameReceived("test") }
        val received = connection.expectTextFrame("test frame")
        assertEquals(received.text, "test", "The message of the forwarded frame should match the received frame")
    }

    @Test
    fun shouldCompleteNormallyWhenUnderlyingFramesFlowCompletes() = runTest {
        val baseConnection = WebSocketConnectionMock()
        val baseClient = webSocketClientMock { baseConnection }

        val reconnectingClient = baseClient.withAutoReconnect { reconnectContext = testDispatcher }
        val connection = reconnectingClient.connect("dummy")

        launch {
            baseConnection.simulateClose(code = WebSocketCloseCodes.NORMAL_CLOSURE, reason = "some reason")
        }
        val received = connection.expectCloseFrame()
        assertEquals(received.code, WebSocketCloseCodes.NORMAL_CLOSURE, "The close code should match")
        assertEquals(received.reason, "some reason", "The close reason should match")
        connection.expectNoMoreFrames()
    }

    @Test
    fun shouldReconnectAndForwardFramesFromNewConnection() = runTest {
        val connections = mutableListOf<WebSocketConnectionMock>()
        val baseClient = webSocketClientMock {
            WebSocketConnectionMock().also {
                connections.add(it) // FYI, exceptions here could be swallowed
            }
        }

        val reconnectingClient = baseClient.withAutoReconnect {
            reconnectContext = testDispatcher
            delayStrategy = FixedDelay(1.milliseconds)
        }
        val connection = reconnectingClient.connect("dummy")
        // if this fails, maybe an exception happened in the connect() method (only visible when reading frames)
        assertEquals(1, connections.size, "base client should have provided 1 connection")

        launch { connections[0].simulateTextFrameReceived("test1") }
        val received1 = connection.expectTextFrame("frame1")
        assertEquals(received1.text, "test1", "The message of the forwarded frame should match the received frame")

        connections[0].simulateError("simulated error")
        advanceUntilIdle() // give time to trigger reconnect coroutine
        assertEquals(2, connections.size, "Should reconnect after error")

        launch { connections[1].simulateTextFrameReceived("test2") }
        val received2 = connection.expectTextFrame("frame2")
        assertNotNull(received2, "The received frame on the second connection should be forwarded")
        assertEquals(received2.text, "test2", "The message of the forwarded frame should match the received frame")
    }

    @Test
    fun shouldCallReconnectCallbackWhenReconnected() = runTest {
        val connections = mutableListOf<WebSocketConnectionMock>()
        val baseClient = webSocketClientMock {
            WebSocketConnectionMock().also {
                connections.add(it)
            }
        }

        var reconnected = false
        val reconnectingClient = baseClient.withAutoReconnect {
            reconnectContext = testDispatcher
            delayStrategy = FixedDelay(1.milliseconds)
            afterReconnect {
                reconnected = true
            }
        }
        val connection = reconnectingClient.connect("dummy")
        assertEquals(1, connections.size, "base client should have provided 1 connection")

        launch { connections[0].simulateTextFrameReceived("test1") }
        val received1 = connection.expectTextFrame("test frame")
        assertEquals(received1.text, "test1", "The message of the forwarded frame should match the received frame")

        assertFalse(reconnected, "afterReconnect callback should not be called after first connection")

        connections[0].simulateError("simulated error")
        advanceUntilIdle() // give time to trigger reconnect coroutine
        assertEquals(2, connections.size, "Should reconnect after error")
        assertTrue(reconnected, "afterReconnect callback should be called")
    }

    @Test
    fun shouldFailAfterMaxAttempts() = runTest {
        val baseClient = ControlledWebSocketClientMock()

        var reconnected = false
        val reconnectingClient = baseClient.withAutoReconnect {
            reconnectContext = testDispatcher
            maxAttempts = 5
            delayStrategy = FixedDelay(100.milliseconds)
            afterReconnect {
                reconnected = true
            }
        }

        val baseConnection = WebSocketConnectionMock()
        launch {
            baseClient.waitForConnectCall()
            baseClient.simulateSuccessfulConnection(baseConnection)
        }
        val connection = reconnectingClient.connect("dummy")

        launch { baseConnection.simulateTextFrameReceived("test1") }
        val received1 = connection.expectTextFrame("test frame")
        assertEquals(received1.text, "test1", "The message of the forwarded frame should match the received frame")

        launch {
            repeat(5) {
                baseClient.waitForConnectCall()
                baseClient.simulateFailedConnection(RuntimeException("error $it"))
            }
        }
        baseConnection.simulateError("simulated error")
        advanceUntilIdle() // give time to trigger reconnect coroutine

        val exception = assertFailsWith(WebSocketReconnectionException::class) { connection.incomingFrames.first() }
        val cause = exception.cause
        assertNotNull(cause, "should provide the last attempt's exception as cause")
        assertEquals("error 4", cause.message)
        assertFalse(reconnected, "afterReconnect callback should not be called")
    }

    @Test
    fun shouldFailIfReconnectPredicateIsFalse() = runTest {
        val baseConnection = WebSocketConnectionMock()
        val baseClient = webSocketClientMock { baseConnection }

        var reconnected = false
        val reconnectingClient = baseClient.withAutoReconnect {
            reconnectContext = testDispatcher
            maxAttempts = 5
            delayStrategy = FixedDelay(100.milliseconds)
            reconnectWhen { _, _ -> false }
            afterReconnect {
                reconnected = true
            }
        }

        val connection = reconnectingClient.connect("dummy")

        launch { baseConnection.simulateTextFrameReceived("test1") }
        val received1 = connection.expectTextFrame("test frame")
        assertEquals(received1.text, "test1", "The message of the forwarded frame should match the received frame")

        baseConnection.simulateError("simulated error")
        advanceUntilIdle() // give time to trigger reconnect coroutine

        val exception = assertFailsWith(WebSocketException::class) { connection.incomingFrames.first() }
        assertEquals("simulated error", exception.message)
        assertFalse(reconnected, "afterReconnect callback should not be called")
    }

    @Test
    fun shouldFailWhenReconnectPredicateBecomesFalse() = runTest {
        val baseClient = ControlledWebSocketClientMock()
        val reconnectingClient = baseClient.withAutoReconnect {
            reconnectContext = testDispatcher
            delayStrategy = FixedDelay(100.milliseconds)
            reconnectWhen { _, attempt -> attempt < 2 }
        }

        val baseConnection = WebSocketConnectionMock()
        launch {
            baseClient.waitForConnectCall()
            baseClient.simulateSuccessfulConnection(baseConnection)
        }

        val connection = reconnectingClient.connect("dummy")

        launch { baseConnection.simulateTextFrameReceived("test1") }
        val received1 = connection.expectTextFrame("test frame 1")
        assertEquals(received1.text, "test1", "The message of the forwarded frame should match the received frame (1)")

        launch {
            baseClient.waitForConnectCall()
            baseClient.simulateFailedConnection(RuntimeException("connection failure 1"))
            baseClient.waitForConnectCall()
            baseClient.simulateFailedConnection(RuntimeException("connection failure 2"))
        }
        baseConnection.simulateError("simulated error 1")
        advanceUntilIdle() // give time to trigger reconnect coroutine

        val exception = assertFailsWith(RuntimeException::class) { connection.incomingFrames.first() }
        assertEquals("connection failure 2", exception.message)
    }
}

private val TestScope.testDispatcher
    get() = coroutineContext[ContinuationInterceptor] ?: EmptyCoroutineContext
