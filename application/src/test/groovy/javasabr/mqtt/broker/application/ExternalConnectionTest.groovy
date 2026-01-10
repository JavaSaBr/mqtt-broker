package javasabr.mqtt.broker.application

import com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3ConnAckException
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode
import javasabr.mqtt.model.MqttProperties
import spock.lang.Ignore

import java.util.concurrent.CompletionException

class ExternalConnectionTest extends IntegrationSpecification {

  def "client should connect to broker without user and pass using MQTT 3.1.1"() {
    given:
        def client = buildExternalMqtt311Client()
    when:
        def result = client.connect().join()
    then:
        result.returnCode == Mqtt3ConnAckReturnCode.SUCCESS
        !result.sessionPresent
    cleanup:
        client.disconnect().join()
  }

  def "client should connect to broker without user and pass using MQTT 5"() {
    given:
        def client = buildExternalMqtt5Client()
    when:
        def result = client.connect().join()
    then:
        result.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
        !result.sessionExpiryInterval.present
        result.serverKeepAlive.present
        result.serverKeepAlive.getAsInt() == MqttProperties.SERVER_KEEP_ALIVE_DISABLED
        !result.serverReference.present
        !result.responseInformation.present
        !result.assignedClientIdentifier.present
        !result.sessionPresent
    cleanup:
        client.disconnect().join()
  }

  def "client should connect to broker with user and pass using MQTT 3.1.1"() {
    given:
        def client = buildExternalMqtt311Client()
    when:
        def result = connectWith(client, 'user1', 'password')
    then:
        result.returnCode == Mqtt3ConnAckReturnCode.SUCCESS
        !result.sessionPresent
    cleanup:
        client.disconnect().join()
  }

  def "client should connect to broker with user and pass using MQTT 5"() {
    given:
        def client = buildExternalMqtt5Client()
    when:
        def result = connectWith(client, 'user1', 'password')
    then:
        result.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
        !result.sessionExpiryInterval.present
        result.serverKeepAlive.present
        result.serverKeepAlive.getAsInt() == MqttProperties.SERVER_KEEP_ALIVE_DISABLED
        !result.serverReference.present
        !result.responseInformation.present
        !result.assignedClientIdentifier.present
        !result.sessionPresent
    cleanup:
        client.disconnect().join()
  }

  def "client should not connect to broker without providing a client id using MQTT 3.1.1"() {
    given:
        def client = buildExternalMqtt311Client("")
    when:
        client.connect().join()
    then:
        def ex = thrown CompletionException
        def cause = ex.cause as Mqtt3ConnAckException
        cause.mqttMessage.returnCode == Mqtt3ConnAckReturnCode.IDENTIFIER_REJECTED
  }

  @Ignore("until finalizing clientId validation")
  def "client should connect to broker without providing a client id using MQTT 5"() {
    given:
        def client = buildExternalMqtt5Client("")
    when:
        def result = client.connect().join()
    then:
        result.reasonCode == Mqtt5ConnAckReasonCode.SUCCESS
        result.assignedClientIdentifier.present
        result.assignedClientIdentifier.get().toString() != ""
    cleanup:
        client.disconnect().join()
  }

  def "client should not connect to broker with invalid client id using MQTT 3.1.1"(String clientId) {
    given:
        def client = buildExternalMqtt311Client(clientId)
    when:
        client.connect().join()
    then:
        def ex = thrown CompletionException
        def cause = ex.cause as Mqtt3ConnAckException
        cause.mqttMessage.returnCode == Mqtt3ConnAckReturnCode.IDENTIFIER_REJECTED
    where:
        clientId << ["!@#!@*()^&"]
  }

  def "client should not connect to broker with invalid client id using MQTT 5"(String clientId) {
    given:
        def client = buildExternalMqtt5Client(clientId)
    when:
        client.connect().join()
    then:
        def ex = thrown CompletionException
        def cause = ex.cause as Mqtt5ConnAckException
        cause.mqttMessage.reasonCode == Mqtt5ConnAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID
    where:
        clientId << ["!@#!@*()^&"]
  }

  def "client should not connect to broker with wrong pass using MQTT 3.1.1"() {
    given:
        def client = buildExternalMqtt311Client()
    when:
        connectWith(client, "user", "wrongPassword")
    then:
        def ex = thrown CompletionException
        def cause = ex.cause as Mqtt3ConnAckException
        cause.mqttMessage.returnCode == Mqtt3ConnAckReturnCode.BAD_USER_NAME_OR_PASSWORD
  }

  def "client should not connect to broker with wrong pass using mqtt 5"() {
    given:
        def client = buildExternalMqtt5Client()
    when:
        connectWith(client, "user", "wrongPassword")
    then:
        def ex = thrown CompletionException
        def cause = ex.cause as Mqtt5ConnAckException
        cause.mqttMessage.reasonCode == Mqtt5ConnAckReasonCode.BAD_USER_NAME_OR_PASSWORD
  }
}
