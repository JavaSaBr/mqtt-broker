package javasabr.mqtt.legacy.network.in

import javasabr.mqtt.legacy.network.packet.in.ConnectAckInPacket
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.PacketProperty
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode
import javasabr.rlib.common.util.ArrayUtils
import javasabr.rlib.common.util.BufferUtils

class ConnectAckInPacketTest extends BaseInPacketTest {

  def "should read packet correctly as mqtt 3.1.1"() {

    given:

        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putBoolean(sessionPresent)
          it.put(ConnectAckReasonCode.NOT_AUTHORIZED.mqtt311)
        }

    when:
        def packet = new ConnectAckInPacket(0b0010_0000 as byte)
        def result = packet.read(mqtt311Connection, dataBuffer, dataBuffer.limit())
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
        packet.maximumPacketSize == MqttProperties.MAXIMUM_PACKET_SIZE_UNDEFINED
        packet.serverKeepAlive == MqttProperties.SERVER_KEEP_ALIVE_UNDEFINED
        packet.sessionExpiryInterval == MqttProperties.SESSION_EXPIRY_INTERVAL_UNDEFINED
        packet.topicAliasMaximum == MqttProperties.TOPIC_ALIAS_MAXIMUM_UNDEFINED
        packet.receiveMax == MqttProperties.RECEIVE_MAXIMUM_UNDEFINED
  }

  def "should read packet correctly as mqtt 5.0"() {

    given:

        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(PacketProperty.REASON_STRING, reasonString)
          it.putProperty(PacketProperty.SERVER_REFERENCE, serverReference)
          it.putProperty(PacketProperty.ASSIGNED_CLIENT_IDENTIFIER, clientId)
          it.putProperty(PacketProperty.AUTHENTICATION_DATA, authData)
          it.putProperty(PacketProperty.AUTHENTICATION_METHOD, authMethod)
          it.putProperty(PacketProperty.MAXIMUM_PACKET_SIZE, maximumPacketSize)
          it.putProperty(PacketProperty.MAXIMUM_QOS, QoS.AT_LEAST_ONCE.ordinal())
          it.putProperty(PacketProperty.RECEIVE_MAXIMUM, receiveMaximum)
          it.putProperty(PacketProperty.RETAIN_AVAILABLE, retainAvailable)
          it.putProperty(PacketProperty.RESPONSE_INFORMATION, responseInformation)
          it.putProperty(PacketProperty.SERVER_KEEP_ALIVE, serverKeepAlive)
          it.putProperty(PacketProperty.SESSION_EXPIRY_INTERVAL, sessionExpiryInterval)
          it.putProperty(PacketProperty.SHARED_SUBSCRIPTION_AVAILABLE, sharedSubscriptionAvailable)
          it.putProperty(PacketProperty.WILDCARD_SUBSCRIPTION_AVAILABLE, wildcardSubscriptionAvailable)
          it.putProperty(PacketProperty.SUBSCRIPTION_IDENTIFIER_AVAILABLE, subscriptionIdAvailable)
          it.putProperty(PacketProperty.TOPIC_ALIAS_MAXIMUM, topicAliasMaximum)
        }

        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putBoolean(sessionPresent)
          it.put(ConnectAckReasonCode.PAYLOAD_FORMAT_INVALID.mqtt5)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }

    when:
        def packet = new ConnectAckInPacket(0b0010_0000 as byte)
        def result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reasonCode == ConnectAckReasonCode.PAYLOAD_FORMAT_INVALID
        packet.sessionPresent == sessionPresent
        packet.serverReference == serverReference
        packet.reason == reasonString
        packet.assignedClientId == clientId
        packet.authenticationData == authData
        packet.authenticationMethod == authMethod
        packet.maximumPacketSize == maximumPacketSize
        packet.maximumQos == QoS.AT_LEAST_ONCE
        packet.receiveMax == receiveMaximum
        packet.retainAvailable == retainAvailable
        packet.responseInformation == responseInformation
        packet.serverKeepAlive == serverKeepAlive
        packet.sessionExpiryInterval == sessionExpiryInterval
        packet.sharedSubscriptionAvailable == sharedSubscriptionAvailable
        packet.wildcardSubscriptionAvailable == wildcardSubscriptionAvailable
        packet.subscriptionIdAvailable == subscriptionIdAvailable
        packet.topicAliasMaximum == topicAliasMaximum

    when:

        propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(PacketProperty.SHARED_SUBSCRIPTION_AVAILABLE, sharedSubscriptionAvailable)
          it.putProperty(PacketProperty.WILDCARD_SUBSCRIPTION_AVAILABLE, wildcardSubscriptionAvailable)
          it.putProperty(PacketProperty.SUBSCRIPTION_IDENTIFIER_AVAILABLE, subscriptionIdAvailable)
        }

        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putBoolean(sessionPresent)
          it.put(ConnectAckReasonCode.PACKET_TOO_LARGE.mqtt5)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }

        packet = new ConnectAckInPacket(0b0010_0000 as byte)
        result = packet.read(mqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reasonCode == ConnectAckReasonCode.PACKET_TOO_LARGE
        packet.sharedSubscriptionAvailable == sharedSubscriptionAvailable
        packet.wildcardSubscriptionAvailable == wildcardSubscriptionAvailable
        packet.subscriptionIdAvailable == subscriptionIdAvailable

  }
}
