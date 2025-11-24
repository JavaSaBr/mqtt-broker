package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode
import javasabr.rlib.common.util.ArrayUtils
import javasabr.rlib.common.util.BufferUtils

class ConnectAckMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read packet correctly as mqtt 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putBoolean(sessionPresent)
          it.put(ConnectAckReasonCode.NOT_AUTHORIZED.mqtt311)
        }
    when:
        def packet = new ConnectAckMqttInMessage(0b0010_0000 as byte)
        def result = packet.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reasonCode == ConnectAckReasonCode.NOT_AUTHORIZED
        packet.sessionPresent == sessionPresent
        packet.serverReference == ""
        packet.reason == ""
        packet.assignedClientId == ""
        packet.authenticationData == ArrayUtils.EMPTY_BYTE_ARRAY
        packet.authenticationMethod == ""
        packet.maximumQos == QoS.EXACTLY_ONCE
        packet.retainAvailable == MqttProperties.RETAIN_AVAILABLE_DEFAULT
        packet.sharedSubscriptionAvailable == MqttProperties.SHARED_SUBSCRIPTION_AVAILABLE_DEFAULT
        packet.wildcardSubscriptionAvailable == MqttProperties.WILDCARD_SUBSCRIPTION_AVAILABLE_DEFAULT
        packet.subscriptionIdAvailable == MqttProperties.SUBSCRIPTION_IDENTIFIER_AVAILABLE_DEFAULT
        packet.responseInformation == ""
        packet.maxMessageSize == MqttProperties.MAXIMUM_MESSAGE_SIZE_UNDEFINED
        packet.serverKeepAlive == MqttProperties.SERVER_KEEP_ALIVE_UNDEFINED
        packet.sessionExpiryInterval == MqttProperties.SESSION_EXPIRY_INTERVAL_UNDEFINED
        packet.topicAliasMaxValue == MqttProperties.TOPIC_ALIAS_MAXIMUM_UNDEFINED
        packet.receiveMaxPublishes == MqttProperties.RECEIVE_MAXIMUM_PUBLISHES_IS_NOT_SET
  }

  def "should read packet correctly as mqtt 5.0"() {
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
          it.put(ConnectAckReasonCode.PAYLOAD_FORMAT_INVALID.mqtt5)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def packet = new ConnectAckMqttInMessage(0b0010_0000 as byte)
        def result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reasonCode == ConnectAckReasonCode.PAYLOAD_FORMAT_INVALID
        packet.sessionPresent == sessionPresent
        packet.serverReference == serverReference
        packet.reason == reasonString
        packet.assignedClientId == mqtt311ClientId
        packet.authenticationData == authData
        packet.authenticationMethod == authMethod
        packet.maxMessageSize == maxPacketSize
        packet.maximumQos == QoS.AT_LEAST_ONCE
        packet.receiveMaxPublishes == receiveMaxPublishes
        packet.retainAvailable == retainAvailable
        packet.responseInformation == responseInformation
        packet.serverKeepAlive == serverKeepAlive
        packet.sessionExpiryInterval == sessionExpiryInterval
        packet.sharedSubscriptionAvailable == sharedSubscriptionAvailable
        packet.wildcardSubscriptionAvailable == wildcardSubscriptionAvailable
        packet.subscriptionIdAvailable == subscriptionIdAvailable
        packet.topicAliasMaxValue == topicAliasMaxValue

    when:
        propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.SHARED_SUBSCRIPTION_AVAILABLE, sharedSubscriptionAvailable)
          it.putProperty(MqttMessageProperty.WILDCARD_SUBSCRIPTION_AVAILABLE, wildcardSubscriptionAvailable)
          it.putProperty(MqttMessageProperty.SUBSCRIPTION_IDENTIFIER_AVAILABLE, subscriptionIdAvailable)
        }
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putBoolean(sessionPresent)
          it.put(ConnectAckReasonCode.PACKET_TOO_LARGE.mqtt5)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
        packet = new ConnectAckMqttInMessage(0b0010_0000 as byte)
        result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reasonCode == ConnectAckReasonCode.PACKET_TOO_LARGE
        packet.sharedSubscriptionAvailable == sharedSubscriptionAvailable
        packet.wildcardSubscriptionAvailable == wildcardSubscriptionAvailable
        packet.subscriptionIdAvailable == subscriptionIdAvailable
  }
}
