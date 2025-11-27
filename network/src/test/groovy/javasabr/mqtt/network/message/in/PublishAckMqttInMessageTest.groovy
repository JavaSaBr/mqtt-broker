package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.exception.MalformedProtocolMqttException
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.rlib.common.util.BufferUtils

class PublishAckMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read message correctly as MQTT 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
        }
    when:
        def inMessage = new PublishAckMqttInMessage(PublishAckMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.exception() == null
        inMessage.reason() == null
        inMessage.messageId() == messageId
        inMessage.reasonCode() == PublishAckReasonCode.SUCCESS
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
          it.put(PublishAckReasonCode.PAYLOAD_FORMAT_INVALID)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def inMessage = new PublishAckMqttInMessage(PublishAckMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.exception() == null
        inMessage.reason() == reasonString
        inMessage.messageId() == messageId
        inMessage.reasonCode() == PublishAckReasonCode.PAYLOAD_FORMAT_INVALID
        inMessage.userProperties() == userProperties
    when:
        def dataBuffer2 = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.put(PublishAckReasonCode.UNSPECIFIED_ERROR)
          it.putMbi(0)
        }
        def inMessage2 = new PublishAckMqttInMessage(PublishAckMqttInMessage.MESSAGE_FLAGS)
        def result2 = inMessage2.read(defaultMqtt5Connection, dataBuffer2, dataBuffer2.limit())
    then:
        result2
        inMessage2.exception() == null
        inMessage2.reason() == null
        inMessage2.messageId() == messageId
        inMessage2.reasonCode() == PublishAckReasonCode.UNSPECIFIED_ERROR
        inMessage2.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
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
        def inMessage = new PublishAckMqttInMessage(PublishAckMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Property:[$MqttMessageProperty.REASON_STRING] is already presented in message:[$MqttMessageType.PUBLISH_ACK]"
  }

  def "should not allow invalid message flags"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.put(PublishAckReasonCode.SUCCESS)
          it.putMbi(0)
        }
    when:
        def inMessage = new PublishAckMqttInMessage(0b0101_0101 as byte)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Unexpected message flags:[0b0101_0101] in message:[$MqttMessageType.PUBLISH_ACK]"
  }
}
