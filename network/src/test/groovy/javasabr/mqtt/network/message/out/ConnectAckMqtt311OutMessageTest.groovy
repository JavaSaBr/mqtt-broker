package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode
import javasabr.mqtt.network.message.in.ConnectAckMqttInMessage
import javasabr.mqtt.network.message.in.PublishAckMqttInMessage
import javasabr.rlib.common.util.BufferUtils
import javasabr.rlib.common.util.NumberUtils

class ConnectAckMqtt311OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new ConnectAckMqtt311OutMessage(
            ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD,
            sessionPresent)
    when:
        def typeAndFlags = outMessage.messageTypeAndFlags()
        byte type = NumberUtils.getHighByteBits(typeAndFlags);
        byte info = NumberUtils.getLowByteBits(typeAndFlags);
    then:
        MqttMessageType.fromByte(type) == MqttMessageType.CONNECT_ACK
        info == PublishAckMqttInMessage.MESSAGE_FLAGS
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt311Connection, it)
        }
        def reader = new ConnectAckMqttInMessage(info)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD
        reader.sessionPresent() == sessionPresent
        reader.assignedClientId() == null
        reader.reason() == null
        reader.responseInformation() == null
        reader.serverReference() == null
        reader.authenticationMethod() == null
        reader.authenticationData() == null
        reader.wildcardSubscriptionAvailable() == MqttProperties.WILDCARD_SUBSCRIPTION_AVAILABLE_IS_NOT_SET
        reader.subscriptionIdAvailable() == MqttProperties.SUBSCRIPTION_IDENTIFIER_AVAILABLE_IS_NOT_SET
        reader.sharedSubscriptionAvailable() == MqttProperties.SHARED_SUBSCRIPTION_AVAILABLE_IS_NOT_SET
        reader.retainAvailable() == MqttProperties.RETAIN_AVAILABLE_IS_NOT_SET
        reader.topicAliasMaxValue() == MqttProperties.TOPIC_ALIAS_MAXIMUM_IS_NOT_SET
        reader.serverKeepAlive() == MqttProperties.SERVER_KEEP_ALIVE_IS_NOT_SET
        reader.receiveMaxPublishes() == MqttProperties.RECEIVE_MAXIMUM_PUBLISHES_IS_NOT_SET
        reader.sessionExpiryInterval() == MqttProperties.SESSION_EXPIRY_INTERVAL_IS_NOT_SET
        reader.maxMessageSize() == MqttProperties.MAXIMUM_MESSAGE_SIZE_IS_NOT_SET
        reader.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
  }
}
