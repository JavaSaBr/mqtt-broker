package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode
import javasabr.mqtt.network.message.in.ConnectAckMqttInMessage
import javasabr.rlib.common.util.BufferUtils

class ConnectAckMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write packet correctly"() {
    given:
        def clientConfig = clientConnectionConfig(
            maxQos,
            MqttVersion.MQTT_5,
            240,
            250,
            maxPacketSize,
            300,
            30,
            false,
            false,
            sessionsEnabled,
            retainAvailable,
            wildcardSubscriptionAvailable,
            subscriptionIdAvailable,
            sharedSubscriptionAvailable);
        def requestedClientId = "-1"
        def requestedSessionExpireInterval = 360
        def requestedKeepAlive = 120
        def requestedReceiveMaxPublishes = 500
        def packet = new ConnectAckMqtt5OutMessage(
            clientConfig,
            ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD,
            sessionPresent,
            mqtt311ClientId,
            requestedClientId,
            requestedSessionExpireInterval,
            requestedKeepAlive,
            requestedReceiveMaxPublishes,
            reasonString,
            serverReference,
            responseInformation,
            authMethod,
            authData,
            userProperties)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt5Connection, it)
        }
        def reader = new ConnectAckMqttInMessage(0b0010_0000 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode == ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD
        reader.sessionPresent == sessionPresent
        reader.retainAvailable == retainAvailable
        reader.sessionExpiryInterval == 240
        reader.receiveMaxPublishes == 250
        reader.maxPacketSize == maxPacketSize
        reader.assignedClientId == mqtt311ClientId
        reader.topicAliasMaxValue == 300
        reader.reason == reasonString
        reader.userProperties() == userProperties
        reader.wildcardSubscriptionAvailable == wildcardSubscriptionAvailable
        reader.subscriptionIdAvailable == subscriptionIdAvailable
        reader.sharedSubscriptionAvailable == sharedSubscriptionAvailable
        reader.serverKeepAlive == 30
        reader.responseInformation == responseInformation
        reader.serverReference == serverReference
        reader.authenticationData == authData
        reader.authenticationMethod == authMethod
  }
}
