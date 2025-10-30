package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode
import javasabr.mqtt.network.message.in.ConnectAckMqttInMessage
import javasabr.rlib.common.util.BufferUtils

class ConnectAckMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def clientConfig = clientConnectionConfig(
            defaultServerConnectionConfig(),
            maxQos,
            MqttVersion.MQTT_5,
            240,
            250,
            maxPacketSize,
            300,
            30,
            false,
            false);
        def requestedClientId = "-1"
        def requestedSessionExpireInterval = 360
        def requestedKeepAlive = 120
        def requestedReceiveMaxPublishes = 500
        def outMessage = new ConnectAckMqtt5OutMessage(
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
          outMessage.write(defaultMqtt5Connection, it)
        }
        def inMessage = new ConnectAckMqttInMessage(0b0010_0000 as byte)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.reasonCode() == ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD
        inMessage.sessionPresent() == sessionPresent
        inMessage.retainAvailable() == retainAvailable
        inMessage.sessionExpiryInterval() == 240
        inMessage.receiveMaxPublishes() == 250
        inMessage.maxPacketSize() == maxPacketSize
        inMessage.assignedClientId() == mqtt311ClientId
        inMessage.topicAliasMaxValue() == 300
        inMessage.reason() == reasonString
        inMessage.userProperties() == userProperties
        inMessage.wildcardSubscriptionAvailable() == wildcardSubscriptionAvailable
        inMessage.subscriptionIdAvailable() == subscriptionIdAvailable
        inMessage.sharedSubscriptionAvailable() == sharedSubscriptionAvailable
        inMessage.serverKeepAlive() == 30
        inMessage.responseInformation() == responseInformation
        inMessage.serverReference() == serverReference
        inMessage.authenticationData() == authData
        inMessage.authenticationMethod() == authMethod
  }
}
