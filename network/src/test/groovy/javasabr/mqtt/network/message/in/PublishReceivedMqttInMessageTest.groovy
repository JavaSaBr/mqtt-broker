package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode
import javasabr.rlib.common.util.BufferUtils

class PublishReceivedMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read packet correctly as mqtt 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
        }
    when:
        def packet = new PublishReceivedMqttInMessage(0b0101_0000 as byte)
        def result = packet.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reason() == ""
        packet.messageId() == messageId
        packet.reasonCode() == PublishReceivedReasonCode.SUCCESS
        packet.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
  }

  def "should read packet correctly as mqtt 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.REASON_STRING, reasonString)
          it.putProperty(MqttMessageProperty.USER_PROPERTY, userProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.put(PublishReceivedReasonCode.QUOTA_EXCEEDED.code() as byte)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def packet = new PublishReceivedMqttInMessage(0b0101_0000 as byte)
        def result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reason() == reasonString
        packet.messageId() == messageId
        packet.reasonCode() == PublishReceivedReasonCode.QUOTA_EXCEEDED
        packet.userProperties() == userProperties
    when:
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.put(PublishReceivedReasonCode.IMPLEMENTATION_SPECIFIC_ERROR.code() as byte)
          it.putMbi(0)
        }
        packet = new PublishReceivedMqttInMessage(0b0101_0000 as byte)
        result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reason() == ""
        packet.messageId() == messageId
        packet.reasonCode() == PublishReceivedReasonCode.IMPLEMENTATION_SPECIFIC_ERROR
        packet.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
  }
}
