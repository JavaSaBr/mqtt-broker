package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.exception.ConnectionRejectException
import javasabr.mqtt.model.exception.MalformedProtocolMqttException
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode
import javasabr.rlib.common.util.ArrayUtils
import javasabr.rlib.common.util.BufferUtils

class ConnectMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read message correctly as MQTT 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString("MQTT")
          it.put(4 as byte)
          it.put(0b11000010 as byte)
          it.putShort(testKeepAlive as short)
          it.putString(mqtt311ClientId)
          it.putString(testUserName)
          it.putBytes(testUserPassword)
        }
    when:
        def inMessage = new ConnectMqttInMessage(ConnectMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        with(inMessage) {
          clientId() == mqtt311ClientId
          mqttVersion() == MqttVersion.MQTT_3_1_1
          password() == testUserPassword
          username() == testUserName
          willTopic() == ""
          willQos() == 0
          willPayload() == ArrayUtils.EMPTY_BYTE_ARRAY
          sessionExpiryInterval() == MqttProperties.SESSION_EXPIRY_INTERVAL_INFINITY
        }
  }

  def "should correctly validate header"(
      String protocolName,
      byte protocolVersion,
      byte headerVariables,
      Exception expectedError) {
    given:
        def testClientId = "someClientId"
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString(protocolName)
          it.put(protocolVersion)
          it.put(headerVariables)
          it.putShort(testKeepAlive as short)
          it.putString(testClientId)
          it.putString(testUserName)
          it.putBytes(testUserPassword)
        }
    when:
        def inMessage = new ConnectMqttInMessage(ConnectMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        inMessage.exception() == expectedError
    where:
        protocolName | protocolVersion | headerVariables | expectedError
        "Invalid"    | 4               | 0b11000010      | new ConnectionRejectException(ConnectAckReasonCode.UNSUPPORTED_PROTOCOL_VERSION)
        "MQTT"       | 2               | 0b11000010      | new ConnectionRejectException(ConnectAckReasonCode.UNSUPPORTED_PROTOCOL_VERSION)
        "MQTT"       | 9               | 0b11000010      | new ConnectionRejectException(ConnectAckReasonCode.UNSUPPORTED_PROTOCOL_VERSION)
        "MQTT"       | 4               | 0b11000011      | new ConnectionRejectException(ConnectAckReasonCode.MALFORMED_PACKET)
        "MQTT"       | 4               | 0b01000010      | new ConnectionRejectException(ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD)
  }

  def "should read message correctly as MQTT 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.SESSION_EXPIRY_INTERVAL, testSessionExpiryInterval)
          it.putProperty(MqttMessageProperty.RECEIVE_MAXIMUM_PUBLISHES, testReceiveMaxPublishes)
          it.putProperty(MqttMessageProperty.MAXIMUM_MESSAGE_SIZE, testMaxMessageSize)
          it.putProperty(MqttMessageProperty.TOPIC_ALIAS_MAXIMUM, testTopicAliasMaxValue)
          it.putProperty(MqttMessageProperty.REQUEST_RESPONSE_INFORMATION, requestResponseInformation ? 1 : 0)
          it.putProperty(MqttMessageProperty.REQUEST_PROBLEM_INFORMATION, requestProblemInformation ? 1 : 0)
          it.putProperty(MqttMessageProperty.AUTHENTICATION_METHOD, testAuthMethod)
          it.putProperty(MqttMessageProperty.AUTHENTICATION_DATA, testAuthData)
          it.putProperty(MqttMessageProperty.USER_PROPERTY, testUserProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString("MQTT")
          it.put(5 as byte)
          it.put(0b11000010 as byte)
          it.putShort(testKeepAlive as short)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.putString(mqtt5ClientId)
          it.putString(testUserName)
          it.putBytes(testUserPassword)
        }
    when:
        def inMessage = new ConnectMqttInMessage(ConnectMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        with(inMessage) {
          keepAlive() == testKeepAlive
          authenticationMethod() == testAuthMethod
          authenticationData() == testAuthData
          clientId() == mqtt5ClientId
          mqttVersion() == MqttVersion.MQTT_5
          maxMessageSize() == testMaxMessageSize
          password() == testUserPassword
          username() == testUserName
          topicAliasMaxValue() == testTopicAliasMaxValue
          sessionExpiryInterval() == testSessionExpiryInterval
          receiveMaxPublishes() == testReceiveMaxPublishes
          willTopic() == ""
          willQos() == 0
          willPayload() == ArrayUtils.EMPTY_BYTE_ARRAY
          userProperties() == testUserProperties
        }
  }

  def "should return disabled session expiry interval when it's not set in MQTT 5.0"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString("MQTT")
          it.put(5 as byte)
          it.put(0b11000010 as byte)
          it.putShort(testKeepAlive as short)
          it.putMbi(0)
          it.putString(mqtt5ClientId)
          it.putString(testUserName)
          it.putBytes(testUserPassword)
        }
    when:
        def inMessage = new ConnectMqttInMessage(ConnectMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        with(inMessage) {
          keepAlive() == testKeepAlive
          clientId() == mqtt5ClientId
          mqttVersion() == MqttVersion.MQTT_5
          password() == testUserPassword
          username() == testUserName
          sessionExpiryInterval() == MqttProperties.SESSION_EXPIRY_INTERVAL_DISABLED
        }
  }

  def "should not read message correctly with invalid UTF8 strings"(byte[] stringBytes) {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString("MQTT")
          it.put(5 as byte)
          it.put(0b11000010 as byte)
          it.putShort(testKeepAlive as short)
          it.putMbi(0)
          it.putBytes(stringBytes)
          it.putString(testUserName)
          it.putBytes(testUserPassword)
        }
    when:
        def packet = new ConnectMqttInMessage(ConnectMqttInMessage.MESSAGE_FLAGS)
        def result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        MalformedProtocolMqttException.isCase(packet.exception())
    where:
        stringBytes << [
            // https://www.cl.cam.ac.uk/~mgk25/ucs/examples/UTF-8-test.txt
            [0xF4, 0x90, 0x80, 0x80] as byte[],
            [0xFE, 0xFE, 0xFF, 0xFF] as byte[],
            [0xFC, 0x80, 0x80, 0x80, 0x80, 0xAF] as byte[],
            [0xED, 0xAF, 0xBF, 0xED, 0xBF, 0xBF] as byte[],
        ]
  }
}
