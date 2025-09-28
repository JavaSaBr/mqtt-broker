package javasabr.mqtt.legacy.network.out

import javasabr.mqtt.legacy.network.packet.in.ConnectAckInPacket
import javasabr.mqtt.legacy.network.packet.out.ConnectAck311OutPacket
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.ArrayUtils
import javasabr.rlib.common.util.BufferUtils

class ConnectAck311OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {

    given:

        def packet = new ConnectAck311OutPacket(
            ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD,
            sessionPresent
        )

    when:

        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(it)
        }

        def reader = new ConnectAckInPacket(0b0010_0000 as byte)
        def result = reader.read(mqtt311Connection, dataBuffer, dataBuffer.limit())

    then:
        result
        reader.reasonCode == ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD
        reader.sessionPresent == sessionPresent
        reader.assignedClientId == ""
        reader.reason == ""
        reader.userProperties == Array.empty()
        reader.retainAvailable == MqttProperties.RETAIN_AVAILABLE_DEFAULT
        reader.wildcardSubscriptionAvailable == MqttProperties.WILDCARD_SUBSCRIPTION_AVAILABLE_DEFAULT
        reader.subscriptionIdAvailable == MqttProperties.SUBSCRIPTION_IDENTIFIER_AVAILABLE_DEFAULT
        reader.sharedSubscriptionAvailable == MqttProperties.SHARED_SUBSCRIPTION_AVAILABLE_DEFAULT
        reader.responseInformation == ""
        reader.serverReference == ""
        reader.authenticationData == ArrayUtils.EMPTY_BYTE_ARRAY
        reader.authenticationMethod == ""
        reader.topicAliasMaximum == MqttProperties.TOPIC_ALIAS_MAXIMUM_UNDEFINED
        reader.serverKeepAlive == MqttProperties.SERVER_KEEP_ALIVE_UNDEFINED
        reader.receiveMax == MqttProperties.RECEIVE_MAXIMUM_UNDEFINED
        reader.sessionExpiryInterval == MqttProperties.SESSION_EXPIRY_INTERVAL_UNDEFINED
        reader.maximumPacketSize == MqttProperties.MAXIMUM_PACKET_SIZE_UNDEFINED
  }
}
