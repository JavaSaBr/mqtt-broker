package javasabr.mqtt.application.network.out

import javasabr.mqtt.network.packet.in.ConnectAckInPacket
import javasabr.mqtt.network.packet.out.ConnectAck5OutPacket
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode
import javasabr.rlib.common.util.BufferUtils

class ConnectAck5OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {

    given:

        def packet = new ConnectAck5OutPacket(
            ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD,
            sessionPresent,
            "-1",
            MqttProperties.SESSION_EXPIRY_INTERVAL_UNDEFINED,
            MqttProperties.SERVER_KEEP_ALIVE_UNDEFINED,
            MqttProperties.TOPIC_ALIAS_MAXIMUM_UNDEFINED,
            reasonString,
            serverReference,
            responseInformation,
            authMethod,
            authData,
            userProperties,
            clientId,
            maxQos,
            sessionExpiryInterval,
            maximumPacketSize,
            receiveMaximum,
            topicAliasMaximum,
            serverKeepAlive,
            retainAvailable,
            wildcardSubscriptionAvailable,
            subscriptionIdAvailable,
            sharedSubscriptionAvailable
        )

    when:

        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(mqtt5Connection, it)
        }

        def reader = new ConnectAckInPacket(0b0010_0000 as byte)
        def result = reader.read(mqtt5Connection, dataBuffer, dataBuffer.limit())

    then:
        result
        reader.reasonCode == ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD
        reader.sessionPresent == sessionPresent
        reader.retainAvailable == retainAvailable
        reader.sessionExpiryInterval == sessionExpiryInterval
        reader.receiveMax == receiveMaximum
        reader.maximumPacketSize == maximumPacketSize
        reader.assignedClientId == clientId
        reader.topicAliasMaximum == topicAliasMaximum
        reader.reason == reasonString
        reader.userProperties == userProperties
        reader.wildcardSubscriptionAvailable == wildcardSubscriptionAvailable
        reader.subscriptionIdAvailable == subscriptionIdAvailable
        reader.sharedSubscriptionAvailable == sharedSubscriptionAvailable
        reader.serverKeepAlive == serverKeepAlive
        reader.responseInformation == responseInformation
        reader.serverReference == serverReference
        reader.authenticationData == authData
        reader.authenticationMethod == authMethod
  }
}
