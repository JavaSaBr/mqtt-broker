package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode
import javasabr.rlib.common.util.ArrayUtils
import javasabr.rlib.common.util.BufferUtils

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
          it.putProperty(MqttMessageProperty.AUTHENTICATION_DATA, authData)
          it.putProperty(MqttMessageProperty.AUTHENTICATION_METHOD, authMethod)
          it.putProperty(MqttMessageProperty.MAXIMUM_MESSAGE_SIZE, maxPacketSize)
          it.putProperty(MqttMessageProperty.MAXIMUM_QOS, QoS.AT_LEAST_ONCE.ordinal())
          it.putProperty(MqttMessageProperty.RECEIVE_MAXIMUM_PUBLISHES, receiveMaxPublishes)
          it.putProperty(MqttMessageProperty.RETAIN_AVAILABLE, retainAvailable)
          it.putProperty(MqttMessageProperty.RESPONSE_INFORMATION, responseInformation)
          it.putProperty(MqttMessageProperty.SERVER_KEEP_ALIVE, serverKeepAlive)
          it.putProperty(MqttMessageProperty.SESSION_EXPIRY_INTERVAL, sessionExpiryInterval)
          it.putProperty(MqttMessageProperty.SHARED_SUBSCRIPTION_AVAILABLE, sharedSubscriptionAvailable)
          it.putProperty(MqttMessageProperty.WILDCARD_SUBSCRIPTION_AVAILABLE, wildcardSubscriptionAvailable)
          it.putProperty(MqttMessageProperty.SUBSCRIPTION_IDENTIFIER_AVAILABLE, subscriptionIdAvailable)
          it.putProperty(MqttMessageProperty.TOPIC_ALIAS_MAXIMUM, topicAliasMaxValue)
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
        inMessage.authenticationData() == authData
        inMessage.authenticationMethod() == authMethod
        inMessage.maxMessageSize() == maxPacketSize
        inMessage.maxQos() == QoS.AT_LEAST_ONCE
        inMessage.receiveMaxPublishes() == receiveMaxPublishes
        (inMessage.retainAvailable() == 1) == retainAvailable
        inMessage.responseInformation() == responseInformation
        inMessage.serverKeepAlive() == serverKeepAlive
        inMessage.sessionExpiryInterval() == sessionExpiryInterval
        (inMessage.sharedSubscriptionAvailable() == 1) == sharedSubscriptionAvailable
        (inMessage.wildcardSubscriptionAvailable() == 1) == wildcardSubscriptionAvailable
        (inMessage.subscriptionIdAvailable() == 1) == subscriptionIdAvailable
        inMessage.topicAliasMaxValue() == topicAliasMaxValue

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
        (inMessage2.sharedSubscriptionAvailable() == 1) == sharedSubscriptionAvailable
        (inMessage2.wildcardSubscriptionAvailable() == 1) == wildcardSubscriptionAvailable
        (inMessage2.subscriptionIdAvailable() == 1) == subscriptionIdAvailable
  }
}