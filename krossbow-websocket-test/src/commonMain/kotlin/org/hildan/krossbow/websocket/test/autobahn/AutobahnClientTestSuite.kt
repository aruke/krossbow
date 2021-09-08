package org.hildan.krossbow.websocket.test.autobahn

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.hildan.krossbow.websocket.*
import org.hildan.krossbow.websocket.test.getDefaultAutobahnTestServerHost
import org.hildan.krossbow.websocket.test.getDefaultAutobahnTestServerPort
import org.hildan.krossbow.websocket.test.runSuspendingTest
import kotlin.test.*

/**
 * Runs tests from the [Autobahn test suite](https://github.com/crossbario/autobahn-testsuite).
 *
 * This test suite requires a running Autobahn fuzzing server. It can be started with the following docker command:
 * ```
 * docker run -it --rm -v "./autobahn/config:/config" -v "./build/autobahn/reports:/reports" -p 12345:12345 crossbario/autobahn-testsuite
 * ```
 * Alternatively, Gradle can be configured to start the relevant container when running tests.
 * See Krossbow's repository for an example setup using `com.avast.gradle.docker-compose` Gradle plugin.
 */
abstract class AutobahnClientTestSuite(
    private val agentUnderTest: String,
    private val testServerUrl: String,
) {
    private lateinit var autobahnClientTester: AutobahnClientTester

    constructor(
        agentUnderTest: String,
        testServerHost: String = getDefaultAutobahnTestServerHost() ?: "localhost",
        testServerPort: Int = getDefaultAutobahnTestServerPort() ?: 9001,
    ) : this(agentUnderTest, testServerUrl = "ws://$testServerHost:$testServerPort")

    abstract fun provideClient(): WebSocketClient

    @BeforeTest
    fun setupClient() {
        autobahnClientTester = AutobahnClientTester(provideClient(), testServerUrl, agentUnderTest)
    }

    @Test
    fun autobahn_1_1_1_echo_text_payload() = runAutobahnTestCase("1.1.1")

    @Test
    fun autobahn_1_1_2_echo_text_payload() = runAutobahnTestCase("1.1.2")

    @Test
    fun autobahn_1_1_3_echo_text_payload() = runAutobahnTestCase("1.1.3")

    @Test
    fun autobahn_1_1_4_echo_text_payload() = runAutobahnTestCase("1.1.4")

    @Test
    fun autobahn_1_1_5_echo_text_payload() = runAutobahnTestCase("1.1.5")

    @Test
    fun autobahn_1_1_6_echo_text_payload() = runAutobahnTestCase("1.1.6")

    @Test
    fun autobahn_1_1_7_echo_text_payload() = runAutobahnTestCase("1.1.7")

    @Test
    fun autobahn_1_1_8_echo_text_payload() = runAutobahnTestCase("1.1.8")

    @Test
    fun autobahn_1_2_1_echo_binary_payload() = runAutobahnTestCase("1.2.1")

    @Test
    fun autobahn_1_2_2_echo_binary_payload() = runAutobahnTestCase("1.2.2")

    @Test
    fun autobahn_1_2_3_echo_binary_payload() = runAutobahnTestCase("1.2.3")

    @Test
    fun autobahn_1_2_4_echo_binary_payload() = runAutobahnTestCase("1.2.4")

    @Test
    fun autobahn_1_2_5_echo_binary_payload() = runAutobahnTestCase("1.2.5")

    @Test
    fun autobahn_1_2_6_echo_binary_payload() = runAutobahnTestCase("1.2.6")

    @Test
    fun autobahn_1_2_7_echo_binary_payload() = runAutobahnTestCase("1.2.7")

    @Test
    fun autobahn_1_2_8_echo_binary_payload() = runAutobahnTestCase("1.2.8")

    @Test
    fun autobahn_2_1_ping_pong() = runAutobahnTestCase("2.1")

    @Test
    fun autobahn_2_2_ping_pong() = runAutobahnTestCase("2.2")

    @Test
    fun autobahn_2_3_ping_pong() = runAutobahnTestCase("2.3")

    @Test
    fun autobahn_2_4_ping_pong() = runAutobahnTestCase("2.4")

    @Test
    fun autobahn_2_5_ping_pong() = runAutobahnTestCase("2.5")

    @Test
    fun autobahn_2_6_ping_pong() = runAutobahnTestCase("2.6")

    @Test
    fun autobahn_2_7_ping_pong() = runAutobahnTestCase("2.7")

    @Test
    fun autobahn_2_8_ping_pong() = runAutobahnTestCase("2.8")

    @Test
    fun autobahn_2_9_ping_pong() = runAutobahnTestCase("2.9")

    @Test
    fun autobahn_2_10_ping_pong() = runAutobahnTestCase("2.10")

    @Test
    fun autobahn_2_11_ping_pong() = runAutobahnTestCase("2.11")

    @Test
    fun autobahn_3_1_reserved_bits() = runAutobahnTestCase("3.1")

    @Test
    fun autobahn_3_2_reserved_bits() = runAutobahnTestCase("3.2")

    @Test
    fun autobahn_3_3_reserved_bits() = runAutobahnTestCase("3.3")

    @Test
    fun autobahn_3_4_reserved_bits() = runAutobahnTestCase("3.4")

    @Test
    fun autobahn_3_5_reserved_bits() = runAutobahnTestCase("3.5")

    @Test
    fun autobahn_3_6_reserved_bits() = runAutobahnTestCase("3.6")

    @Test
    fun autobahn_3_7_reserved_bits() = runAutobahnTestCase("3.7")

    @Test
    fun autobahn_4_1_1_opcodes() = runAutobahnTestCase("4.1.1")

    @Test
    fun autobahn_4_1_2_opcodes() = runAutobahnTestCase("4.1.2")

    @Test
    fun autobahn_4_1_3_opcodes() = runAutobahnTestCase("4.1.3")

    @Test
    fun autobahn_4_1_4_opcodes() = runAutobahnTestCase("4.1.4")

    @Test
    fun autobahn_4_1_5_opcodes() = runAutobahnTestCase("4.1.5")

    @Test
    fun autobahn_4_2_1_opcodes() = runAutobahnTestCase("4.2.1")

    @Test
    fun autobahn_4_2_2_opcodes() = runAutobahnTestCase("4.2.2")

    @Test
    fun autobahn_4_2_3_opcodes() = runAutobahnTestCase("4.2.3")

    @Test
    fun autobahn_4_2_4_opcodes() = runAutobahnTestCase("4.2.4")

    @Test
    fun autobahn_4_2_5_opcodes() = runAutobahnTestCase("4.2.5")

    @Test
    fun autobahn_5_1_echo_payload() = runAutobahnTestCase("5.1")

    @Test
    fun autobahn_5_2_echo_payload() = runAutobahnTestCase("5.2")

    @Test
    fun autobahn_5_3_echo_payload() = runAutobahnTestCase("5.3")

    @Test
    fun autobahn_5_4_echo_payload() = runAutobahnTestCase("5.4")

    @Test
    fun autobahn_5_5_echo_payload() = runAutobahnTestCase("5.5")

    @Test
    fun autobahn_5_6_echo_payload() = runAutobahnTestCase("5.6")

    @Test
    fun autobahn_5_7_echo_payload() = runAutobahnTestCase("5.7")

    @Test
    fun autobahn_5_8_echo_payload() = runAutobahnTestCase("5.8")

    @Test
    fun autobahn_5_9_echo_payload() = runAutobahnTestCase("5.9")

    @AfterTest
    fun generateReports() = runSuspendingTest {
        autobahnClientTester.updateReports()
    }

    private fun runAutobahnTestCase(caseId: String) = runSuspendingTest {
        runAutobahnTestCase(AUTOBAHN_CASES.single { it.id == caseId })
        val status = autobahnClientTester.getCaseStatus(caseId)
        val testResultAcceptable = status.behavior == TestCaseStatus.OK || status.behavior == TestCaseStatus.NON_STRICT
        assertTrue(testResultAcceptable, "Test case $caseId finished with status ${status.behavior}, expected OK or NON-STRICT")
    }

    private suspend fun runAutobahnTestCase(case: AutobahnCase) {
        try {
            val session = withTimeoutOrFail(1000, case, stepDescription = "connection to server") {
                autobahnClientTester.connectForAutobahnTestCase(case.id)
            }
            withTimeoutOrFail(5000, case, stepDescription = "echo loop") {
                session.echoUntilClosed()
            }
        } catch (e: WebSocketException) {
            assertTrue(case.expectFailure, "Unexpected exception during test case ${case.id}: $e")
        } catch (e: Exception) {
            val message = "Exception during test case ${case.id} (expected: ${case.expectFailure}):\n${e.stackTraceToString()}"
            // TODO check the actual failure more specifically, we might not expect *this one*
            //  (different WS implementations may throw different exceptions, though)
            if (!case.expectFailure) {
                fail(message)
            }
        }
    }
}

private suspend fun WebSocketConnection.echoUntilClosed() {
    incomingFrames.receiveAsFlow().takeWhile { it !is WebSocketFrame.Close }.collect {
        echoFrame(it)
    }
}

private suspend fun <T> withTimeoutOrFail(
    timeoutMs: Long,
    testCase: AutobahnCase,
    stepDescription: String,
    block: suspend () -> T,
): T = withTimeoutOrNull(timeoutMs) { block() } ?: fail("Test case ${testCase.id} timed out (step: $stepDescription)")

private suspend fun WebSocketConnection.echoNFrames(n: Int) {
    repeat(n) {
        val frame = incomingFrames.receive()
        echoFrame(frame)
    }
}

private suspend fun WebSocketConnection.echoFrame(frame: WebSocketFrame) {
    when (frame) {
        is WebSocketFrame.Text -> sendText(frame.text)
        is WebSocketFrame.Binary -> sendBinary(frame.bytes)
        is WebSocketFrame.Ping -> Unit // nothing special, we expect the underlying impl to PONG properly
        is WebSocketFrame.Pong -> Unit // nothing to do
        is WebSocketFrame.Close -> error("should not receive CLOSE frame at that point")
    }
}

/*
The following methods could be used for a more precise behaviour check.
 */

private suspend fun WebSocketConnection.echoExactFrameCountAndExpectClosure(case: AutobahnCase) {
    // FIXME properly expect number of data frames, pings and pongs
    withTimeoutOrFail(3000, case, "echo frames") {
        echoNFrames(case.nExpectedFramesBeforeEnd)
    }
    withTimeoutOrFail(1000, case, "closing connection") {
        when (case.end) {
            CaseEnd.CLIENT_FORCE_CLOSE -> expectClientForceClosed()
            CaseEnd.SERVER_CLOSE -> expectServerClosed()
        }
    }
}

private suspend fun WebSocketConnection.expectClientForceClosed() {
    // Note: the channel might be closed, or a frame could be received before the client had time to force close, or
    // a frame could be received as part of the test, etc.
    // We can't really rely on channel closure generally, hence this "weak" check that works on all client-close tests
    val result = withTimeoutOrNull(2000) {
        incomingFrames.receiveAsFlow().toList()
    }
    assertFalse(result == null, "Incoming frames channel should not hang (the client should have closed the connection)")
}

private suspend fun WebSocketConnection.expectServerClosed() {
    val frames = incomingFrames.receiveAsFlow().toList()
    // ping-pongs are irrelevant because they are not echoed (they are more low-level frames)
    val relevantFrames = frames.filter { !(it is WebSocketFrame.Pong || it is WebSocketFrame.Ping) }
    assertEquals(1, relevantFrames.size, "The server should only have sent a single frame by now (CLOSE), got ${relevantFrames.map { it.truncated(20) }}")
    assertIs<WebSocketFrame.Close>(relevantFrames.single(), "The frame received from the server should be a CLOSE frame")
    assertTrue(incomingFrames.receiveCatching().isClosed, "The incoming frames channel should be closed by now")
}

private fun WebSocketFrame.truncated(length: Int) = if (this is WebSocketFrame.Text) {
    copy(text = text.take(length))
} else {
    this
}