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
import javasabr.mqtt.broker.application.ApplicationPropertiesSpecification
import javasabr.mqtt.broker.application.config.MqttBrokerTestConfig

import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletionException

class AuthenticationServiceTest extends ApplicationPropertiesSpecification {

  def setup() {
    applyProperties(MqttBrokerTestConfig, "application-test.properties")
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
        String[] authenticationProperties = new String[]{
            "authentication.allow.anonymous=false",
            "authentication.provider=$provider",
            "authentication.credentials.source=$source"
        }
    expect:
        runContextWithApplicationProperties(authenticationProperties, this.&buildMqtt311Client) { Mqtt3AsyncClient subscriber ->
          // when
          try {
            subscriber.connect(connectMessage).join()
            throw new AssertionError("MQTT 3 client is able to connect with wrong password" as Object)
            // then
          } catch (CompletionException e) {
            assert e.cause instanceof Mqtt3ConnAckException
            def connAckEx = e.cause as Mqtt3ConnAckException
            assert connAckEx.mqttMessage.returnCode == Mqtt3ConnAckReturnCode.BAD_USER_NAME_OR_PASSWORD
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
        runContextWithApplicationProperties(properties, this.&buildMqtt5Client) { Mqtt5AsyncClient subscriber ->
          // when
          try {
            subscriber.connect(connectMessage).join()
            throw new AssertionError("MQTT 5 client is able to connect with wrong password" as Object)
            // then
          } catch (CompletionException e) {
            assert e.cause instanceof Mqtt5ConnAckException
            def connAckEx = e.cause as Mqtt5ConnAckException
            assert connAckEx.mqttMessage.reasonCode == Mqtt5ConnAckReasonCode.BAD_USER_NAME_OR_PASSWORD
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
        runContextWithApplicationProperties(properties, this.&buildMqtt311Client) { Mqtt3AsyncClient subscriber ->
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
        runContextWithApplicationProperties(properties, this.&buildMqtt5Client) { Mqtt5AsyncClient subscriber ->
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
        runContextWithApplicationProperties(properties, this.&buildMqtt5Client) { Mqtt5AsyncClient subscriber ->
          // when
          try {
            subscriber.connect(connectMessage).join()
            throw new AssertionError("MQTT 5 client is able to connect with blank username" as Object)
            // then
          } catch (CompletionException e) {
            assert e.cause instanceof Mqtt5ConnAckException
            def connAckEx = e.cause as Mqtt5ConnAckException
            assert connAckEx.mqttMessage.reasonCode == Mqtt5ConnAckReasonCode.BAD_USER_NAME_OR_PASSWORD
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
        runContextWithApplicationProperties(properties, this.&buildMqtt311Client) { Mqtt3AsyncClient subscriber ->
          // when
          try {
            subscriber.connect(connectMessage).join()
            throw new AssertionError("MQTT 5 client is able to connect with blank username" as Object)
            // then
          } catch (CompletionException e) {
            assert e.cause instanceof Mqtt3ConnAckException
            def connAckEx = e.cause as Mqtt3ConnAckException
            assert connAckEx.mqttMessage.returnCode == Mqtt3ConnAckReturnCode.BAD_USER_NAME_OR_PASSWORD
          }
        }
    where:
        source     | provider
        "file"     | "basic"
        "database" | "basic"
  }
}