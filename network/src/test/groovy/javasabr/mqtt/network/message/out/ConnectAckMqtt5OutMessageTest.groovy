package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode
import javasabr.mqtt.network.message.in.ConnectAckMqttInMessage
import javasabr.mqtt.network.message.in.PublishAckMqttInMessage
import javasabr.rlib.common.util.BufferUtils
import javasabr.rlib.common.util.NumberUtils

class ConnectAckMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def serverConnectionConfig = defaultServerConnectionConfig()
            .withSubscriptionIdAvailable(false)
            .withSharedSubscriptionAvailable(false)
            .withWildcardSubscriptionAvailable(false)
            .withRetainAvailable(false)
        def clientConfig = clientConnectionConfig(
            serverConnectionConfig,
            QoS.EXACTLY_ONCE,
            MqttVersion.MQTT_5,
            240,
            250,
            maxMessageSize,
            300,
            30,
            false,
            false);
        def connection = mqttConnection(
            serverConnectionConfig,
            clientConfig,
            mqtt5ClientId)
        def requestedClientId = "-1"
        def requestedSessionExpireInterval = 360
        def requestedKeepAlive = 120
        def requestedReceiveMaxPublishes = 500
        def outMessage = new ConnectAckMqtt5OutMessage(
            ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD,
            sessionPresent,
            mqtt5ClientId,
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
        def typeAndFlags = outMessage.messageTypeAndFlags()
        byte type = NumberUtils.getHighByteBits(typeAndFlags);
        byte info = NumberUtils.getLowByteBits(typeAndFlags);
    then:
        MqttMessageType.fromByte(type) == MqttMessageType.CONNECT_ACK
        info == PublishAckMqttInMessage.MESSAGE_FLAGS
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(connection, it)
        }
        def inMessage = new ConnectAckMqttInMessage(info)
        def result = inMessage.read(connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.reasonCode() == ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD
        inMessage.sessionPresent() == sessionPresent
        inMessage.sessionExpiryInterval() == 240
        inMessage.receiveMaxPublishes() == 250
        inMessage.maxMessageSize() == maxMessageSize
        inMessage.assignedClientId() == mqtt5ClientId
        inMessage.topicAliasMaxValue() == 300
        inMessage.reason() == reasonString
        inMessage.userProperties() == userProperties
        inMessage.serverKeepAlive() == 30
        inMessage.responseInformation() == responseInformation
        inMessage.serverReference() == serverReference
        inMessage.authenticationData() == authData
        inMessage.authenticationMethod() == authMethod
        !NumberUtils.toBoolean(inMessage.wildcardSubscriptionAvailable())
        !NumberUtils.toBoolean(inMessage.subscriptionIdAvailable())
        !NumberUtils.toBoolean(inMessage.sharedSubscriptionAvailable())
        !NumberUtils.toBoolean(inMessage.retainAvailable())
  }
}
