package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttProtocolErrors
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.exception.MalformedProtocolMqttException
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode
import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.rlib.common.util.BufferUtils
import javasabr.rlib.common.util.NumberUtils

class ConnectAckMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read message correctly as MQTT 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putBoolean(sessionPresent)
          it.putByte(ConnectAckReasonCode.NOT_AUTHORIZED.mqtt311())
        }
    when:
        def inMessage = new ConnectAckMqttInMessage(ConnectAckMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.reasonCode() == ConnectAckReasonCode.NOT_AUTHORIZED
        inMessage.sessionPresent() == sessionPresent
        inMessage.serverReference() == null
        inMessage.reason() == null
        inMessage.assignedClientId() == null
        inMessage.authenticationData() == null
        inMessage.authenticationMethod() == null
        inMessage.responseInformation() == null
        inMessage.maxQos() == null
        inMessage.retainAvailable() == MqttProperties.RETAIN_AVAILABLE_IS_NOT_SET
        inMessage.sharedSubscriptionAvailable() == MqttProperties.SHARED_SUBSCRIPTION_AVAILABLE_IS_NOT_SET
        inMessage.wildcardSubscriptionAvailable() == MqttProperties.WILDCARD_SUBSCRIPTION_AVAILABLE_IS_NOT_SET
        inMessage.subscriptionIdAvailable() == MqttProperties.SUBSCRIPTION_IDENTIFIER_AVAILABLE_IS_NOT_SET
        inMessage.maxMessageSize() == MqttProperties.MAXIMUM_MESSAGE_SIZE_IS_NOT_SET
        inMessage.serverKeepAlive() == MqttProperties.SERVER_KEEP_ALIVE_IS_NOT_SET
        inMessage.sessionExpiryInterval() == MqttProperties.SESSION_EXPIRY_INTERVAL_IS_NOT_SET
        inMessage.topicAliasMaxValue() == MqttProperties.TOPIC_ALIAS_MAXIMUM_IS_NOT_SET
        inMessage.receiveMaxPublishes() == MqttProperties.RECEIVE_MAXIMUM_PUBLISHES_IS_NOT_SET
  }

  def "should read message correctly as MQTT 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.REASON_STRING, reasonString)
          it.putProperty(MqttMessageProperty.SERVER_REFERENCE, serverReference)
          it.putProperty(MqttMessageProperty.ASSIGNED_CLIENT_IDENTIFIER, mqtt311ClientId)
          it.putProperty(MqttMessageProperty.AUTHENTICATION_DATA, testAuthData)
          it.putProperty(MqttMessageProperty.AUTHENTICATION_METHOD, testAuthMethod)
          it.putProperty(MqttMessageProperty.MAXIMUM_MESSAGE_SIZE, testMaxMessageSize)
          it.putProperty(MqttMessageProperty.MAXIMUM_QOS, QoS.AT_LEAST_ONCE.ordinal())
          it.putProperty(MqttMessageProperty.RECEIVE_MAXIMUM_PUBLISHES, testReceiveMaxPublishes)
          it.putProperty(MqttMessageProperty.RETAIN_AVAILABLE, retainAvailable)
          it.putProperty(MqttMessageProperty.RESPONSE_INFORMATION, responseInformation)
          it.putProperty(MqttMessageProperty.SERVER_KEEP_ALIVE, serverKeepAlive)
          it.putProperty(MqttMessageProperty.SESSION_EXPIRY_INTERVAL, testSessionExpiryInterval)
          it.putProperty(MqttMessageProperty.SHARED_SUBSCRIPTION_AVAILABLE, sharedSubscriptionAvailable)
          it.putProperty(MqttMessageProperty.WILDCARD_SUBSCRIPTION_AVAILABLE, wildcardSubscriptionAvailable)
          it.putProperty(MqttMessageProperty.SUBSCRIPTION_IDENTIFIER_AVAILABLE, subscriptionIdAvailable)
          it.putProperty(MqttMessageProperty.TOPIC_ALIAS_MAXIMUM, testTopicAliasMaxValue)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putBoolean(sessionPresent)
          it.putByte(ConnectAckReasonCode.PAYLOAD_FORMAT_INVALID.mqtt5())
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def inMessage = new ConnectAckMqttInMessage(ConnectAckMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.reasonCode() == ConnectAckReasonCode.PAYLOAD_FORMAT_INVALID
        inMessage.sessionPresent() == sessionPresent
        inMessage.serverReference() == serverReference
        inMessage.reason() == reasonString
        inMessage.assignedClientId() == mqtt311ClientId
        inMessage.authenticationData() == testAuthData
        inMessage.authenticationMethod() == testAuthMethod
        inMessage.maxMessageSize() == testMaxMessageSize
        inMessage.maxQos() == QoS.AT_LEAST_ONCE
        inMessage.receiveMaxPublishes() == testReceiveMaxPublishes
        inMessage.responseInformation() == responseInformation
        inMessage.serverKeepAlive() == serverKeepAlive
        inMessage.sessionExpiryInterval() == testSessionExpiryInterval
        NumberUtils.toBoolean(inMessage.sharedSubscriptionAvailable()) == sharedSubscriptionAvailable
        NumberUtils.toBoolean(inMessage.wildcardSubscriptionAvailable()) == wildcardSubscriptionAvailable
        NumberUtils.toBoolean(inMessage.subscriptionIdAvailable()) == subscriptionIdAvailable
        NumberUtils.toBoolean(inMessage.retainAvailable()) == retainAvailable
    when:
        def propertiesBuffer2 = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.SHARED_SUBSCRIPTION_AVAILABLE, sharedSubscriptionAvailable)
          it.putProperty(MqttMessageProperty.WILDCARD_SUBSCRIPTION_AVAILABLE, wildcardSubscriptionAvailable)
          it.putProperty(MqttMessageProperty.SUBSCRIPTION_IDENTIFIER_AVAILABLE, subscriptionIdAvailable)
        }
        def dataBuffer2 = BufferUtils.prepareBuffer(512) {
          it.putBoolean(sessionPresent)
          it.putByte(ConnectAckReasonCode.PACKET_TOO_LARGE.mqtt5())
          it.putMbi(propertiesBuffer2.limit())
          it.put(propertiesBuffer2)
        }
        def inMessage2 = new ConnectAckMqttInMessage(ConnectAckMqttInMessage.MESSAGE_FLAGS)
        def result2 = inMessage2.read(defaultMqtt5Connection, dataBuffer2, dataBuffer2.limit())
    then:
        result2
        inMessage2.reasonCode() == ConnectAckReasonCode.PACKET_TOO_LARGE
        NumberUtils.toBoolean(inMessage2.sharedSubscriptionAvailable()) == sharedSubscriptionAvailable
        NumberUtils.toBoolean(inMessage2.wildcardSubscriptionAvailable()) == wildcardSubscriptionAvailable
        NumberUtils.toBoolean(inMessage2.subscriptionIdAvailable()) == subscriptionIdAvailable
  }

  def "should not allow duplicated properties in message"(MqttMessageProperty property, Object value) {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(property, value)
          it.putProperty(property, value)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putBoolean(false)
          it.putByte(ConnectAckReasonCode.SUCCESS.mqtt5())
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def inMessage = new ConnectAckMqttInMessage(ConnectAckMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Property:[$property] is already presented in message:[$MqttMessageType.CONNECT_ACK]"
    where:
        property                                              | value
        MqttMessageProperty.AUTHENTICATION_DATA               | testAuthData
        MqttMessageProperty.ASSIGNED_CLIENT_IDENTIFIER        | mqtt5ClientId
        MqttMessageProperty.REASON_STRING                     | reasonString
        MqttMessageProperty.RESPONSE_INFORMATION              | responseInformation
        MqttMessageProperty.AUTHENTICATION_METHOD             | testAuthMethod
        MqttMessageProperty.SERVER_REFERENCE                  | serverReference
        MqttMessageProperty.WILDCARD_SUBSCRIPTION_AVAILABLE   | wildcardSubscriptionAvailable
        MqttMessageProperty.SHARED_SUBSCRIPTION_AVAILABLE     | sharedSubscriptionAvailable
        MqttMessageProperty.SUBSCRIPTION_IDENTIFIER_AVAILABLE | subscriptionIdAvailable
        MqttMessageProperty.RETAIN_AVAILABLE                  | retainAvailable
        MqttMessageProperty.RECEIVE_MAXIMUM_PUBLISHES         | testReceiveMaxPublishes
        MqttMessageProperty.MAXIMUM_QOS                       | QoS.EXACTLY_ONCE.level()
        MqttMessageProperty.SERVER_KEEP_ALIVE                 | serverKeepAlive
        MqttMessageProperty.TOPIC_ALIAS_MAXIMUM               | testTopicAliasMaxValue
        MqttMessageProperty.SESSION_EXPIRY_INTERVAL           | testSessionExpiryInterval
        MqttMessageProperty.MAXIMUM_MESSAGE_SIZE              | testMaxMessageSize
  }

  def "should validate invalid properties in message"(MqttMessageProperty property, Object value, String expectedMessage) {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(property, value)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putBoolean(false)
          it.putByte(ConnectAckReasonCode.SUCCESS.mqtt5())
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def inMessage = new ConnectAckMqttInMessage(ConnectAckMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == expectedMessage
    where:
        property                                              | value             | expectedMessage
        MqttMessageProperty.WILDCARD_SUBSCRIPTION_AVAILABLE   | 2                 | MqttProtocolErrors.PROVIDED_INVALID_WILDCARD_SUBSCRIPTION_AVAILABLE
        MqttMessageProperty.WILDCARD_SUBSCRIPTION_AVAILABLE   | -1                | MqttProtocolErrors.PROVIDED_INVALID_WILDCARD_SUBSCRIPTION_AVAILABLE
        MqttMessageProperty.SHARED_SUBSCRIPTION_AVAILABLE     | 3                 | MqttProtocolErrors.PROVIDED_INVALID_SHARED_SUBSCRIPTION_AVAILABLE
        MqttMessageProperty.SHARED_SUBSCRIPTION_AVAILABLE     | -2                | MqttProtocolErrors.PROVIDED_INVALID_SHARED_SUBSCRIPTION_AVAILABLE
        MqttMessageProperty.SUBSCRIPTION_IDENTIFIER_AVAILABLE | 4                 | MqttProtocolErrors.PROVIDED_INVALID_SUBSCRIPTION_IDENTIFIERS_AVAILABLE
        MqttMessageProperty.SUBSCRIPTION_IDENTIFIER_AVAILABLE | -3                | MqttProtocolErrors.PROVIDED_INVALID_SUBSCRIPTION_IDENTIFIERS_AVAILABLE
        MqttMessageProperty.RETAIN_AVAILABLE                  | 5                 | MqttProtocolErrors.PROVIDED_INVALID_RETAIN_AVAILABLE
        MqttMessageProperty.RETAIN_AVAILABLE                  | -4                | MqttProtocolErrors.PROVIDED_INVALID_RETAIN_AVAILABLE
        MqttMessageProperty.RECEIVE_MAXIMUM_PUBLISHES         | 0                 | MqttProtocolErrors.PROVIDED_INVALID_RECEIVED_MAX_PUBLISHES
        MqttMessageProperty.MAXIMUM_QOS                       | 3                 | MqttProtocolErrors.PROVIDED_INVALID_MAX_QOS
        MqttMessageProperty.MAXIMUM_QOS                       | -1                | MqttProtocolErrors.PROVIDED_INVALID_MAX_QOS
        MqttMessageProperty.MAXIMUM_MESSAGE_SIZE              | 1                 | MqttProtocolErrors.PROVIDED_INVALID_MAX_MESSAGE_SIZE
        MqttMessageProperty.MAXIMUM_MESSAGE_SIZE              | 300 * 1024 * 1024 | MqttProtocolErrors.PROVIDED_INVALID_MAX_MESSAGE_SIZE
  }

  def "should not allow invalid message flags"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.put(PublishAckReasonCode.SUCCESS)
          it.putMbi(0)
        }
    when:
        def inMessage = new ConnectAckMqttInMessage(0b0101_0101 as byte)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Unexpected message flags:[0b0101_0101] in message:[$MqttMessageType.CONNECT_ACK]"
  }
}
