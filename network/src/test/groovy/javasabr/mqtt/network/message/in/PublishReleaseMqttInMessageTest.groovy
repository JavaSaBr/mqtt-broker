package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class PublishReleaseMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read packet correctly as mqtt 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
        }
    when:
        def packet = new PublishReleaseMqttInMessage(0b0110_0000 as byte)
        def result = packet.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reason() == ""
        packet.messageId() == messageId
        packet.reasonCode() == PublishReleaseReasonCode.SUCCESS
        packet.userProperties() == Array.empty()
  }

  def "should read packet correctly as mqtt 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.REASON_STRING, reasonString)
          it.putProperty(MqttMessageProperty.USER_PROPERTY, userProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.put(PublishReleaseReasonCode.PACKET_IDENTIFIER_NOT_FOUND.value)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def packet = new PublishReleaseMqttInMessage(0b0110_0000 as byte)
        def result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reason() == reasonString
        packet.messageId() == messageId
        packet.reasonCode() == PublishReleaseReasonCode.PACKET_IDENTIFIER_NOT_FOUND
        packet.userProperties() == userProperties
    when:
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.put(PublishReleaseReasonCode.SUCCESS.value)
          it.putMbi(0)
        }
        packet = new PublishReleaseMqttInMessage(0b0110_0000 as byte)
        result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reason() == ""
        packet.messageId() == messageId
        packet.reasonCode() == PublishReleaseReasonCode.SUCCESS
        packet.userProperties() == Array.empty()
  }
}
