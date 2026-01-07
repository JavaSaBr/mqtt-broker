package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttProtocolErrors
import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.exception.ConnectionRejectException
import javasabr.mqtt.model.exception.MalformedProtocolMqttException
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode
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
          cleanStart()
          willTopic() == null
          willQos() == null
          willPayload() == null
          sessionExpiryInterval() == MqttProperties.SESSION_EXPIRY_INTERVAL_INFINITY
        }
  }

  def "should read message correctly with Will Topic as MQTT 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString("MQTT")
          it.put(4 as byte)
          it.put(0b11110100 as byte)
          it.putShort(testKeepAlive as short)
          it.putString(mqtt311ClientId)
          it.putString(testWillTopic.rawTopic())
          it.putBytes(testWillPayload)
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
          !cleanStart()
          willTopic() == testWillTopic.rawTopic()
          willQos() == QoS.EXACTLY_ONCE
          willRetain()
          willPayload() == testWillPayload
          sessionExpiryInterval() == MqttProperties.SESSION_EXPIRY_INTERVAL_INFINITY
        }
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
          willTopic() == null
          willQos() == null
          willPayload() == null
          userProperties() == testUserProperties
        }
  }

  def "should read message correctly with Will Topic as MQTT 5.0"() {
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
        def willPropertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.WILL_DELAY_INTERVAL, testWillDelayInterval)
          it.putProperty(MqttMessageProperty.PAYLOAD_FORMAT_INDICATOR, testWillPayloadFormat)
          it.putProperty(MqttMessageProperty.MESSAGE_EXPIRY_INTERVAL, testWillMessageExpiryInterval)
          it.putProperty(MqttMessageProperty.CONTENT_TYPE, testWillContentType)
          it.putProperty(MqttMessageProperty.RESPONSE_TOPIC, testWillResponseTopic.rawTopic())
          it.putProperty(MqttMessageProperty.CORRELATION_DATA, testWillCorrelationData)
          it.putProperty(MqttMessageProperty.USER_PROPERTY, testWillUserProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString("MQTT")
          it.put(5 as byte)
          it.put(0b11110100 as byte)
          it.putShort(testKeepAlive as short)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.putString(mqtt5ClientId)
          it.putMbi(willPropertiesBuffer.limit())
          it.put(willPropertiesBuffer)
          it.putString(testWillTopic.rawTopic())
          it.putBytes(testWillPayload)
          it.putString(testUserName)
          it.putBytes(testUserPassword)
        }
    when:
        def inMessage = new ConnectMqttInMessage(ConnectMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        with(inMessage) {
          clientId() == mqtt5ClientId
          mqttVersion() == MqttVersion.MQTT_5
          password() == testUserPassword
          username() == testUserName
          !cleanStart()
          willTopic() == testWillTopic.rawTopic()
          willQos() == QoS.EXACTLY_ONCE
          willRetain()
          willPayload() == testWillPayload
          sessionExpiryInterval() == testSessionExpiryInterval
          willContentType() == testWillContentType
          willCorrelationData() == testWillCorrelationData
          willResponseTopic() == testWillResponseTopic.rawTopic()
          willDelayInterval() == testWillDelayInterval
          willPayload() == testWillPayload
          willMessageExpiryInterval() == testWillMessageExpiryInterval
          willDelayInterval() == testWillDelayInterval
          willUserProperties() == testWillUserProperties
        }
  }
  
  def "should correctly validate header"(
      String protocolName,
      byte protocolVersion,
      byte headerVariables,
      ConnectAckReasonCode expectedError) {
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
        with(inMessage.exception() as ConnectionRejectException) {
          reasonCode() == expectedError
        }
    where:
        protocolName | protocolVersion | headerVariables | expectedError
        // invalid protocols
        "Invalid"    | 4               | 0b11000010      | ConnectAckReasonCode.UNSUPPORTED_PROTOCOL_VERSION
        "MQTT"       | 2               | 0b11000010      | ConnectAckReasonCode.UNSUPPORTED_PROTOCOL_VERSION
        "MQTT"       | 9               | 0b11000010      | ConnectAckReasonCode.UNSUPPORTED_PROTOCOL_VERSION
        // not zero reserved bit
        "MQTT"       | 4               | 0b11000011      | ConnectAckReasonCode.MALFORMED_PACKET
        // password is presented without username for MQTT 3.1.1
        "MQTT"       | 4               | 0b01000010      | ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD
        // invalid will QoS
        "MQTT"       | 4               | 0b11011000      | ConnectAckReasonCode.MALFORMED_PACKET
        // not zero will QoS without will flag
        "MQTT"       | 4               | 0b11101000      | ConnectAckReasonCode.MALFORMED_PACKET
        "MQTT"       | 4               | 0b11110000      | ConnectAckReasonCode.MALFORMED_PACKET
        // not zero will retain without will flag
        "MQTT"       | 4               | 0b11100000      | ConnectAckReasonCode.MALFORMED_PACKET
  }

  def "should return infinity session expiry interval for MQTT 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString("MQTT")
          it.put(4 as byte)
          it.put(0b00000010 as byte)
          it.putShort(testKeepAlive as short)
          it.putString(mqtt311ClientId)
        }
    when:
        def inMessage = new ConnectMqttInMessage(ConnectMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        with(inMessage) {
          keepAlive() == testKeepAlive
          clientId() == mqtt311ClientId
          mqttVersion() == MqttVersion.MQTT_3_1_1
          sessionExpiryInterval() == MqttProperties.SESSION_EXPIRY_INTERVAL_INFINITY
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

  def "should not allow duplicated properties in message"(MqttMessageProperty property, Object value) {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(property, value)
          it.putProperty(property, value)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString("MQTT")
          it.put(5 as byte)
          it.put(0b00000010 as byte)
          it.putShort(testKeepAlive as short)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.putString(mqtt5ClientId)
        }
    when:
        def inMessage = new ConnectMqttInMessage(ConnectMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        with(inMessage) {
          exception() instanceof MalformedProtocolMqttException
          exception().message == "Property:[$property] is already presented in message:[$MqttMessageType.CONNECT]"
        }
    where:
        property                                         | value
        MqttMessageProperty.AUTHENTICATION_METHOD        | testAuthMethod
        MqttMessageProperty.AUTHENTICATION_DATA          | testAuthData
        MqttMessageProperty.RECEIVE_MAXIMUM_PUBLISHES    | testReceiveMaxPublishes
        MqttMessageProperty.MAXIMUM_MESSAGE_SIZE         | testMaxMessageSize
        MqttMessageProperty.TOPIC_ALIAS_MAXIMUM          | testTopicAliasMaxValue
        MqttMessageProperty.REQUEST_RESPONSE_INFORMATION | false
        MqttMessageProperty.REQUEST_PROBLEM_INFORMATION  | false
  }

  def "should not allow duplicated will properties in message"(MqttMessageProperty property, Object value) {
    given:
        def willPropertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(property, value)
          it.putProperty(property, value)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString("MQTT")
          it.put(5 as byte)
          it.put(0b0011_0100 as byte)
          it.putShort(testKeepAlive as short)
          it.putMbi(0)
          it.putString(mqtt5ClientId)
          it.putMbi(willPropertiesBuffer.limit())
          it.put(willPropertiesBuffer)
          it.putString(testWillTopic.rawTopic())
          it.putBytes(testWillPayload)
        }
    when:
        def inMessage = new ConnectMqttInMessage(ConnectMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        with(inMessage) {
          exception() instanceof MalformedProtocolMqttException
          exception().message == "Property:[$property] is already presented in message:[$MqttMessageType.CONNECT]"
        }
    where:
        property                                     | value
        MqttMessageProperty.WILL_DELAY_INTERVAL      | testWillDelayInterval
        MqttMessageProperty.PAYLOAD_FORMAT_INDICATOR | testWillPayloadFormat
        MqttMessageProperty.MESSAGE_EXPIRY_INTERVAL  | testWillMessageExpiryInterval
        MqttMessageProperty.CONTENT_TYPE             | testWillContentType
        MqttMessageProperty.RESPONSE_TOPIC           | testWillResponseTopic.rawTopic()
        MqttMessageProperty.CORRELATION_DATA         | testWillCorrelationData
  }

  def "should validate properties in message"(MqttMessageProperty property, Object value, String expectedError) {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(property, value)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString("MQTT")
          it.put(5 as byte)
          it.put(0b00000010 as byte)
          it.putShort(testKeepAlive as short)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.putString(mqtt5ClientId)
        }
    when:
        def inMessage = new ConnectMqttInMessage(ConnectMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        with(inMessage) {
          exception() instanceof MalformedProtocolMqttException
          exception().message == expectedError
        }
    where:
        property                                         | value       | expectedError
        MqttMessageProperty.RECEIVE_MAXIMUM_PUBLISHES    | 0           | MqttProtocolErrors.PROVIDED_INVALID_RECEIVED_MAX_PUBLISHES
        MqttMessageProperty.MAXIMUM_MESSAGE_SIZE         | 0           | MqttProtocolErrors.PROVIDED_INVALID_MAX_MESSAGE_SIZE
        MqttMessageProperty.MAXIMUM_MESSAGE_SIZE         | -1          | MqttProtocolErrors.PROVIDED_INVALID_MAX_MESSAGE_SIZE
        MqttMessageProperty.REQUEST_RESPONSE_INFORMATION | (3 as byte) | MqttProtocolErrors.PROVIDED_INVALID_REQUEST_RESPONSE_INFORMATION
        MqttMessageProperty.REQUEST_PROBLEM_INFORMATION  | (3 as byte) | MqttProtocolErrors.PROVIDED_INVALID_REQUEST_PROBLEM_INFORMATION
  }

  def "should validate will properties in message"(MqttMessageProperty property, Object value, String expectedError) {
    given:
        def willPropertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(property, value)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putString("MQTT")
          it.put(5 as byte)
          it.put(0b0011_0100 as byte)
          it.putShort(testKeepAlive as short)
          it.putMbi(0)
          it.putString(mqtt5ClientId)
          it.putMbi(willPropertiesBuffer.limit())
          it.put(willPropertiesBuffer)
          it.putString(testWillTopic.rawTopic())
          it.putBytes(testWillPayload)
        }
    when:
        def inMessage = new ConnectMqttInMessage(ConnectMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        with(inMessage) {
          exception() instanceof MalformedProtocolMqttException
          exception().message == expectedError
        }
    where:
        property                                     | value | expectedError
        MqttMessageProperty.WILL_DELAY_INTERVAL      | -1    | MqttProtocolErrors.PROVIDED_INVALID_WILL_DELAY_INTERVAL
        MqttMessageProperty.PAYLOAD_FORMAT_INDICATOR | 5     | MqttProtocolErrors.PROVIDED_INVALID_PAYLOAD_FORMAT
  }
}
