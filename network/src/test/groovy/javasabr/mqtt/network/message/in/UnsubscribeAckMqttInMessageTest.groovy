package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode
import javasabr.rlib.common.util.BufferUtils

class UnsubscribeAckMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read packet correctly as mqtt 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
        }
    when:
        def packet = new UnsubscribeAckMqttInMessage(0b1011_0000 as byte)
        def result = packet.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reason() == ""
        packet.messageId() == messageId
        packet.reasonCodes() == MqttInMessage.EMPTY_USER_PROPERTIES
  }

  def "should read packet correctly as mqtt 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.REASON_STRING, reasonString)
          it.putProperty(MqttMessageProperty.USER_PROPERTY, userProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.put(UnsubscribeAckReasonCode.SUCCESS.value)
          it.put(UnsubscribeAckReasonCode.SUCCESS.value)
          it.put(UnsubscribeAckReasonCode.NOT_AUTHORIZED.value)
          it.put(UnsubscribeAckReasonCode.UNSPECIFIED_ERROR.value)
        }
    when:
        def packet = new UnsubscribeAckMqttInMessage(0b1011_0000 as byte)
        def result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reason() == reasonString
        packet.messageId() == messageId
        packet.reasonCodes().size() == 4
        packet.reasonCodes().get(0) == UnsubscribeAckReasonCode.SUCCESS
        packet.reasonCodes().get(1) == UnsubscribeAckReasonCode.SUCCESS
        packet.reasonCodes().get(2) == UnsubscribeAckReasonCode.NOT_AUTHORIZED
        packet.reasonCodes().get(3) == UnsubscribeAckReasonCode.UNSPECIFIED_ERROR
        packet.userProperties() == userProperties
    when:
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(0)
          it.put(UnsubscribeAckReasonCode.UNSPECIFIED_ERROR.value)
          it.put(UnsubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR.value)
        }
        packet = new UnsubscribeAckMqttInMessage(0b1011_0000 as byte)
        result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.reason() == ""
        packet.messageId() == messageId
        packet.reasonCodes().size() == 2
        packet.reasonCodes().get(0) == UnsubscribeAckReasonCode.UNSPECIFIED_ERROR
        packet.reasonCodes().get(1) == UnsubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR
        packet.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
  }
}
