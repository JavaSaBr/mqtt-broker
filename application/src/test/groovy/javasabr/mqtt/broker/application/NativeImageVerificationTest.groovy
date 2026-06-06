package javasabr.mqtt.broker.application

import groovy.util.logging.Slf4j
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification
import spock.util.EmbeddedSpecRunner

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import static spock.util.EmbeddedSpecRunner.XFailure

@Slf4j
@IgnoreIf({ !new File(BINARY_DIR, BINARY_NAME).exists() })
class NativeImageVerificationTest extends Specification {

  private static final String BINARY_DIR = "build/native/nativeCompile"
  private static final String BINARY_NAME = "application"
  private static final String NETWORK_READY_MARKER = "Started external MQTT network by address"

  @Shared
  Process brokerProcess

  @Shared
  EmbeddedSpecRunner testRunner

  def setupSpec() {
    brokerProcess = startBroker()
    testRunner = new EmbeddedSpecRunner(throwFailure: false)
  }

  private static Process startBroker() {
    def binaryDir = new File(BINARY_DIR)
    def applicationPropertiesPath = new File("src/test/resources/application-test.properties").absolutePath
    def binaryPath = new File(binaryDir, BINARY_NAME).absolutePath
    Process process = new ProcessBuilder()
        .command(binaryPath, "--spring.config.location=file://${applicationPropertiesPath}")
        .directory(binaryDir)
        .redirectErrorStream(true)
        .start()

    def networkReady = new CountDownLatch(1)

    Thread.startDaemon("broker-output-drainer") {
      def reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
      String line
      while ((line = reader.readLine()) != null) {
        println(line)
        if (line.contains(NETWORK_READY_MARKER)) {
          networkReady.countDown()
        }
      }
    }

    if (!networkReady.await(10, TimeUnit.SECONDS)) {
      process.destroyForcibly()
      throw new RuntimeException("Broker failed to start within 10 seconds: network not ready")
    }

    return process
  }

  def cleanupSpec() {
    if (brokerProcess != null && brokerProcess.isAlive()) {
      brokerProcess.destroy()
      brokerProcess.waitFor(5, TimeUnit.SECONDS)
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
                "extends IntegrationSpecification",
                "extends javasabr.mqtt.broker.application.NativeImageVerificationTest.WrapperSpec"
            )
    when:
        def testResult = testRunner.run(testSource)
    then:
        with(testResult.properties) { props ->
          props.each { k, v -> println "$k: $v" }
          if (props['failureCount'] > 0) {
            props['failures'].each { XFailure f -> f.exception.printStackTrace() }
          }
          props['testsStartedCount'] > 0
          props['testsStartedCount'] == props['testsSucceededCount']
        }
    where:
        test << ["ConnectSubscribePublishTest", "ExternalConnectionTest", "PublishRetryTest"]
  }

  static class WrapperSpec extends IntegrationSpecification {

    def setup() {
      externalPlainNetworkAddress = InetSocketAddress.createUnresolved("localhost", 1883)
    }
  }
}
