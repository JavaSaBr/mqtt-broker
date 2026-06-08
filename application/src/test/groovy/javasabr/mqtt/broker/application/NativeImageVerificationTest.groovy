package javasabr.mqtt.broker.application

import groovy.util.logging.Slf4j
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.testkit.engine.EngineExecutionResults
import org.junit.platform.testkit.engine.EventType
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification
import spock.util.EmbeddedSpecRunner

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import static javasabr.mqtt.broker.application.NativeImageVerificationTest.ConsoleStyle.color
import static javasabr.mqtt.broker.application.NativeImageVerificationTest.ConsoleStyle.italic
import static spock.util.EmbeddedSpecRunner.XFailure

@Slf4j
@Requires({ new File(BINARY_DIR, BINARY_NAME).exists() })
class NativeImageVerificationTest extends Specification {

  private static final String BINARY_DIR = "build/native/nativeCompile"
  private static final String BINARY_NAME = "application"
  private static final String NETWORK_READY_MARKER = "Started external MQTT network by address"

  @Shared
  Process brokerProcess

  @Shared
  EmbeddedSpecRunner testRunner

  def setupSpec() {
    brokerProcess = new ProcessBuilder()
        .command(buildBrokerStartupCommand())
        .directory(new File(BINARY_DIR))
        .redirectErrorStream(true)
        .start()
    awaitBrokerStartup(brokerProcess, 1, TimeUnit.SECONDS)
    testRunner = new EmbeddedSpecRunner(throwFailure: false)
  }

  def cleanupSpec() {
    try {
      if (brokerProcess != null && brokerProcess.isAlive()) {
        brokerProcess.destroy()
        brokerProcess.waitFor(5, TimeUnit.SECONDS)
      }
    } finally {
      if (brokerProcess.isAlive()) {
        brokerProcess.destroyForcibly()
      }
    }
  }

  def "#test should pass without failures"(String test) {
    given:
        def testSource = new File("src/test/groovy/javasabr/mqtt/broker/application/${test}.groovy")
            .text
            .replace(
                "extends TlsIntegrationSpecification",
                "extends javasabr.mqtt.broker.application.NativeImageVerificationTest.TlsStandaloneBrokerRouterSpec"
            )
            .replace(
                "extends IntegrationSpecification",
                "extends javasabr.mqtt.broker.application.NativeImageVerificationTest.StandaloneBrokerRouterSpec"
            )
    when:
        def testResult = testRunner.run(testSource)
    then:
        with(testResult.properties) { results ->
          printTestResults(results)
          results['testsStartedCount'] > 0
          results['testsStartedCount'] == results['testsSucceededCount']
        }
    where:
        test << ["ConnectSubscribePublishTest", "ExternalConnectionTest", "PublishRetryTest", "TlsCommunicationTest"]
  }


  private static String[] buildBrokerStartupCommand() {
    def binaryDir = new File(BINARY_DIR)
    def applicationPropertiesPath = new File("src/test/resources/application-test.properties").absolutePath
    def binaryPath = new File(binaryDir, BINARY_NAME).absolutePath

    return TestSslPropertiesInitializer.getProps()
        .collect { key, value -> "--${key}=${value}" as String }
        .plus(0, [
            binaryPath,
            "--spring.config.location=file://${applicationPropertiesPath}" as String,
            "--mqtt.external.tls.network.enabled=true",
            "--mqtt.external.tls.require-client-cert=false"
        ])
        .toArray(String[]::new)
  }

  private static void awaitBrokerStartup(Process process, long amount, TimeUnit unit) {
    def networkReady = new CountDownLatch(1)
    Thread.startDaemon("broker-output-drainer") {
      def reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
      String line
      while ((line = reader.readLine()) != null) {
        println "[Standalone Broker] ${line}"
        if (line.contains(NETWORK_READY_MARKER)) {
          networkReady.countDown()
        }
      }
    }
    if (!networkReady.await(amount, unit)) {
      process.destroyForcibly()
      throw new RuntimeException("Broker failed to start within 10 seconds: network not ready")
    }
  }

  static void printTestResults(Map props) {
    (props['results'] as EngineExecutionResults).testEvents()
        .stream()
        .filter { it.type == EventType.FINISHED }
        .each { event ->
          def testMethod = event.testDescriptor.displayName
          def status = event.payload
              .map { it as TestExecutionResult }
              .map { it.status }
              .orElse(null)
          log.info "'${italic(testMethod)}' ${color(status)}"
        }

    if (props['failureCount'] > 0) {
      props['failures'].each { XFailure f -> f.exception.printStackTrace() }
    }
  }

  static class StandaloneBrokerRouterSpec extends IntegrationSpecification {
    def setup() {
      externalPlainNetworkAddress = InetSocketAddress.createUnresolved("localhost", 1883)
    }
  }

  static class TlsStandaloneBrokerRouterSpec extends TlsIntegrationSpecification {
    def setup() {
      externalTlsNetworkAddress = InetSocketAddress.createUnresolved("localhost", 8883)
    }
  }

  enum ConsoleStyle {
    RED("\u001B[31m", TestExecutionResult.Status.FAILED),
    GREEN("\u001B[32m", TestExecutionResult.Status.SUCCESSFUL),
    YELLOW("\u001B[33m", TestExecutionResult.Status.ABORTED);

    static final Map<TestExecutionResult.Status, String> CACHE = values().collectEntries { [it.status, it.anchor] }

    static final String RESET = "\u001B[0m"
    static final String ITALIC = "\u001B[3m"

    String anchor;
    TestExecutionResult.Status status;

    ConsoleStyle(String anchor, TestExecutionResult.Status status) {
      this.anchor = anchor
      this.status = status
    }

    static String color(TestExecutionResult.Status status) {
      return "${CACHE.getOrDefault(status, RESET)}${status}${RESET}"
    }

    static String italic(String text) {
      return "${ITALIC}${text}${RESET}"
    }
  }
}
