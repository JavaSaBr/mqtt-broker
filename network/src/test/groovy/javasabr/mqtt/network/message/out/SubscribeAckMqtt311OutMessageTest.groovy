package javasabr.mqtt.network.message.out

import javasabr.mqtt.network.message.in.MqttInMessage
import javasabr.mqtt.network.message.in.SubscribeAckMqttInMessage
import javasabr.rlib.common.util.BufferUtils

class SubscribeAckMqtt311OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def outMessage = new SubscribeAckMqtt311OutMessage(messageId, subscribeAckReasonCodes)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt311Connection, it)
        }
        def reader = new SubscribeAckMqttInMessage(0 as byte)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.reasonCodes() == subscribeAckReasonCodes
        reader.messageId() == messageId
        reader.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
        reader.reason() == ""
  }
}
