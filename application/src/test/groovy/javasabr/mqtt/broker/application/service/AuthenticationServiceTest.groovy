package javasabr.mqtt.broker.application.service

import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3ConnAckException
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode
import javasabr.mqtt.broker.application.ContextRunnerSpecification
import javasabr.mqtt.broker.application.MqttClientFactory
import javasabr.mqtt.broker.application.config.MqttBrokerTestConfig

import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletionException

class AuthenticationServiceTest extends ContextRunnerSpecification {

  def setup() {
    createContextRunner(MqttBrokerTestConfig, "application-test.properties")
  }

  def "should not be able to connect with wrong password using mqtt 3.1.1 client"() {
    given:
        def existingUsername = "user"
        def wrongPassword = "wrong-password".getBytes(StandardCharsets.UTF_8)
        def connectMessage = Mqtt3Connect.builder()
            .simpleAuth(Mqtt3SimpleAuth.builder()
                .username(existingUsername)
                .password(wrongPassword)
                .build())
            .build()
        String[] properties = new String[]{
            "authentication.allow.anonymous=false",
            "authentication.provider=$provider",
            "authentication.credentials.source=$source"
        }
    expect:
        runContextWithProperties(properties, MqttClientFactory.&buildMqtt311Client) { Mqtt3AsyncClient subscriber ->
          // when
          def exception = { try { subscriber.connect(connectMessage).join() } catch(e) { return e } }()
          // then
          assert exception instanceof CompletionException
          assert exception.cause instanceof Mqtt3ConnAckException
          with(exception.cause as Mqtt3ConnAckException) {
            mqttMessage.returnCode == Mqtt3ConnAckReturnCode.BAD_USER_NAME_OR_PASSWORD
          }
        }
    where:
        source     | provider
        "file"     | "basic"
        "database" | "basic"
  }

  def "should not be able to connect with wrong password using mqtt 5 client"() {
    given:
        def existingUsername = "user"
        def wrongPassword = "wrong-password".getBytes(StandardCharsets.UTF_8)
        def connectMessage = Mqtt5Connect.builder()
            .simpleAuth(Mqtt5SimpleAuth.builder()
                .username(existingUsername)
                .password(wrongPassword)
                .build())
            .build()
        String[] properties = new String[]{
            "authentication.allow.anonymous=false",
            "authentication.provider=$provider",
            "authentication.credentials.source=$source"
        }
    expect:
        runContextWithProperties(properties, MqttClientFactory.&buildMqtt5Client) { Mqtt5AsyncClient subscriber ->
          // when
          def exception = { try { subscriber.connect(connectMessage).join() } catch(e) { return e } }()
          // then
          assert exception instanceof CompletionException
          assert exception.cause instanceof Mqtt5ConnAckException
          with(exception.cause as Mqtt5ConnAckException) {
            mqttMessage.reasonCode == Mqtt5ConnAckReasonCode.BAD_USER_NAME_OR_PASSWORD
          }
        }
    where:
        source     | provider
        "file"     | "basic"
        "database" | "basic"
  }

  def "should be able to connect with correct password using mqtt 3.1.1 client"() {
    given:
        def existingUsername = "user"
        byte[] correctPassword = "correct-password".getBytes(StandardCharsets.UTF_8)
    and:
        def connectMessage = Mqtt3Connect.builder()
            .simpleAuth(Mqtt3SimpleAuth.builder()
                .username(existingUsername)
                .password(correctPassword)
                .build())
            .build()
        String[] properties = new String[]{
            "authentication.allow.anonymous=false",
            "authentication.provider=$provider",
            "authentication.credentials.source=$source"
        }
    expect:
        runContextWithProperties(properties, MqttClientFactory.&buildMqtt311Client) { Mqtt3AsyncClient subscriber ->
          // when
          Mqtt3ConnAck ack = subscriber.connect(connectMessage).join()
          // then
          assert ack instanceof Mqtt3ConnAck
          assert ack.returnCode == Mqtt3ConnAckReturnCode.SUCCESS
          subscriber.disconnect().join()
        }
    where:
        source     | provider
        "file"     | "basic"
        "database" | "basic"
  }

  def "should be able to connect with correct password using mqtt 5 client"() {
    given:
        def existingUsername = "user"
        byte[] correctPassword = "correct-password".getBytes(StandardCharsets.UTF_8)
    and:
        def connectMessage = Mqtt5Connect.builder()
            .simpleAuth(Mqtt5SimpleAuth.builder()
                .username(existingUsername)
                .password(correctPassword)
                .build())
            .build()
        String[] properties = new String[]{
            "authentication.allow.anonymous=false",
            "authentication.provider=$provider",
            "authentication.credentials.source=$source"
        }
    expect:
        runContextWithProperties(properties, MqttClientFactory.&buildMqtt5Client) { Mqtt5AsyncClient subscriber ->
          // when
          Mqtt5ConnAck ack = subscriber.connect(connectMessage).join()
          // then
          assert ack instanceof Mqtt5ConnAck
          assert ack.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
          subscriber.disconnect().join()
        }
    where:
        source     | provider
        "file"     | "basic"
        "database" | "basic"
  }

  def "should not be able to connect without username and with correct password using mqtt 5 client"() {
    given:
        def blankUsername = ""
        def correctPassword = "correct-password".getBytes(StandardCharsets.UTF_8)
    and:
        def connectMessage = Mqtt5Connect.builder()
            .simpleAuth(Mqtt5SimpleAuth.builder()
                .username(blankUsername)
                .password(correctPassword)
                .build())
            .build()
        String[] properties = new String[]{
            "authentication.allow.anonymous=false",
            "authentication.provider=$provider",
            "authentication.credentials.source=$source"
        }
    expect:
        runContextWithProperties(properties, MqttClientFactory.&buildMqtt5Client) { Mqtt5AsyncClient subscriber ->
          // when
          def exception = { try { subscriber.connect(connectMessage).join() } catch(e) { return e } }()
          // then
          assert exception instanceof CompletionException
          assert exception.cause instanceof Mqtt5ConnAckException
          with(exception.cause as Mqtt5ConnAckException) {
            mqttMessage.reasonCode == Mqtt5ConnAckReasonCode.BAD_USER_NAME_OR_PASSWORD
          }
        }
    where:
        source     | provider
        "file"     | "basic"
        "database" | "basic"
  }

  def "should not be able to connect without username and with correct password using mqtt 3.1.1 client"() {
    given:
        def emptyUserName = ""
        def correctPassword = "correct-password".getBytes(StandardCharsets.UTF_8)
    and:
        def connectMessage = Mqtt3Connect.builder()
            .simpleAuth(Mqtt3SimpleAuth.builder()
                .username(emptyUserName)
                .password(correctPassword)
                .build())
            .build()
        String[] properties = new String[]{
            "authentication.allow.anonymous=false",
            "authentication.provider=$provider",
            "authentication.credentials.source=$source"
        }
    expect:
        runContextWithProperties(properties, MqttClientFactory.&buildMqtt311Client) { Mqtt3AsyncClient subscriber ->
          // when
          def exception = { try { subscriber.connect(connectMessage).join() } catch(e) { return e } }()
          // then
          assert exception instanceof CompletionException
          assert exception.cause instanceof Mqtt3ConnAckException
          with(exception.cause as Mqtt3ConnAckException) {
            mqttMessage.returnCode == Mqtt3ConnAckReturnCode.BAD_USER_NAME_OR_PASSWORD
          }
        }
    where:
        source     | provider
        "file"     | "basic"
        "database" | "basic"
  }
}
