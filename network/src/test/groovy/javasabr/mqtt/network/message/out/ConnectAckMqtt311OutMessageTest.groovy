package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode
import javasabr.mqtt.network.message.in.ConnectAckMqttInMessage
import javasabr.rlib.common.util.ArrayUtils
import javasabr.rlib.common.util.BufferUtils

class ConnectAckMqtt311OutMessageTest extends BaseMqttOutMessageTest {

  def "should write packet correctly"() {
    given:
        def packet = new ConnectAckMqtt311OutMessage(
            ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD,
            sessionPresent)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt311Connection, it)
        }
        def reader = new ConnectAckMqttInMessage(0b0010_0000 as byte)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCode() == ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD
        reader.sessionPresent() == sessionPresent
        reader.assignedClientId() == ""
        reader.reason() == ""
        reader.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        reader.retainAvailable() == MqttProperties.RETAIN_AVAILABLE_DEFAULT
        reader.wildcardSubscriptionAvailable() == MqttProperties.WILDCARD_SUBSCRIPTION_AVAILABLE_DEFAULT
        reader.subscriptionIdAvailable() == MqttProperties.SUBSCRIPTION_IDENTIFIER_AVAILABLE_DEFAULT
        reader.sharedSubscriptionAvailable() == MqttProperties.SHARED_SUBSCRIPTION_AVAILABLE_DEFAULT
        reader.responseInformation() == ""
        reader.serverReference() == ""
        reader.authenticationData() == ArrayUtils.EMPTY_BYTE_ARRAY
        reader.authenticationMethod() == ""
        reader.topicAliasMaxValue() == MqttProperties.TOPIC_ALIAS_MAXIMUM_IS_NOT_SET
        reader.serverKeepAlive() == MqttProperties.SERVER_KEEP_ALIVE_IS_NOT_SET
        reader.receiveMaxPublishes() == MqttProperties.RECEIVE_MAXIMUM_PUBLISHES_IS_NOT_SET
        reader.sessionExpiryInterval() == MqttProperties.SESSION_EXPIRY_INTERVAL_IS_NOT_SET
        reader.maxMessageSize() == MqttProperties.MAXIMUM_MESSAGE_SIZE_IS_NOT_SET
  }
}