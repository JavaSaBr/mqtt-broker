package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.exception.MalformedProtocolMqttException
import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode
import javasabr.rlib.common.util.BufferUtils

class PublishReleaseMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read message correctly as MQTT 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
        }
    when:
        def inMessage = new PublishReleaseMqttInMessage(PublishReleaseMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.reason() == null
        inMessage.messageId() == messageId
        inMessage.reasonCode() == PublishReleaseReasonCode.SUCCESS
        inMessage.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
  }

  def "should read message correctly as MQTT 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.REASON_STRING, reasonString)
          it.putProperty(MqttMessageProperty.USER_PROPERTY, userProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.put(PublishReleaseReasonCode.PACKET_IDENTIFIER_NOT_FOUND)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def inMessage = new PublishReleaseMqttInMessage(PublishReleaseMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.reason() == reasonString
        inMessage.messageId() == messageId
        inMessage.reasonCode() == PublishReleaseReasonCode.PACKET_IDENTIFIER_NOT_FOUND
        inMessage.userProperties() == userProperties
    when:
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.put(PublishReleaseReasonCode.SUCCESS)
          it.putMbi(0)
        }
        inMessage = new PublishReleaseMqttInMessage(PublishReleaseMqttInMessage.MESSAGE_FLAGS)
        result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.reason() == null
        inMessage.messageId() == messageId
        inMessage.reasonCode() == PublishReleaseReasonCode.SUCCESS
        inMessage.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
  }

  def "should not allow to put reason 2 times"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.REASON_STRING, "reason2")
          it.putProperty(MqttMessageProperty.REASON_STRING, "reason1")
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.put(PublishAckReasonCode.SUCCESS)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def inMessage = new PublishReleaseMqttInMessage(PublishReleaseMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "[REASON_STRING] is already presented"
  }

  def "should not allow invalid message flags"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.REASON_STRING, "reason2")
          it.putProperty(MqttMessageProperty.REASON_STRING, "reason1")
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.put(PublishAckReasonCode.SUCCESS)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def inMessage = new PublishReleaseMqttInMessage(0b0101_0101 as byte)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Unexpected flags bits:0b0101_0101"
  }
}
