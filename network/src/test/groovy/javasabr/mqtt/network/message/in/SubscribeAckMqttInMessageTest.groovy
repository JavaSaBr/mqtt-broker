package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.exception.MalformedProtocolMqttException
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode
import javasabr.rlib.common.util.BufferUtils

class SubscribeAckMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read message correctly as MQTT 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putByte(SubscribeAckReasonCode.GRANTED_QOS_0.code())
          it.putByte(SubscribeAckReasonCode.GRANTED_QOS_2.code())
          it.putByte(SubscribeAckReasonCode.GRANTED_QOS_1.code())
          it.putByte(SubscribeAckReasonCode.UNSPECIFIED_ERROR.code())
        }
    when:
        def inMessage = new SubscribeAckMqttInMessage(SubscribeAckMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.reason() == null
        inMessage.messageId() == messageId
        inMessage.reasonCodes().size() == 4
        inMessage.reasonCodes().get(0) == SubscribeAckReasonCode.GRANTED_QOS_0
        inMessage.reasonCodes().get(1) == SubscribeAckReasonCode.GRANTED_QOS_2
        inMessage.reasonCodes().get(2) == SubscribeAckReasonCode.GRANTED_QOS_1
        inMessage.reasonCodes().get(3) == SubscribeAckReasonCode.UNSPECIFIED_ERROR
  }

  def "should read message correctly as MQTT 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.REASON_STRING, reasonString)
          it.putProperty(MqttMessageProperty.USER_PROPERTY, userProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.putByte(SubscribeAckReasonCode.GRANTED_QOS_0.code())
          it.putByte(SubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR.code())
          it.putByte(SubscribeAckReasonCode.GRANTED_QOS_1.code())
          it.putByte(SubscribeAckReasonCode.UNSPECIFIED_ERROR.code())
        }
    when:
        def inMessage = new SubscribeAckMqttInMessage(SubscribeAckMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.reason() == reasonString
        inMessage.messageId() == messageId
        inMessage.reasonCodes().size() == 4
        inMessage.reasonCodes().get(0) == SubscribeAckReasonCode.GRANTED_QOS_0
        inMessage.reasonCodes().get(1) == SubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR
        inMessage.reasonCodes().get(2) == SubscribeAckReasonCode.GRANTED_QOS_1
        inMessage.reasonCodes().get(3) == SubscribeAckReasonCode.UNSPECIFIED_ERROR
        inMessage.userProperties() == userProperties
    when:
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(0)
          it.putByte(SubscribeAckReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED.code())
          it.putByte(SubscribeAckReasonCode.GRANTED_QOS_2.code())
          it.putByte(SubscribeAckReasonCode.GRANTED_QOS_1.code())
          it.putByte(SubscribeAckReasonCode.UNSPECIFIED_ERROR.code())
        }
        inMessage = new SubscribeAckMqttInMessage(SubscribeAckMqttInMessage.MESSAGE_FLAGS)
        result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.reason() == null
        inMessage.messageId() == messageId
        inMessage.reasonCodes().size() == 4
        inMessage.reasonCodes().get(0) == SubscribeAckReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED
        inMessage.reasonCodes().get(1) == SubscribeAckReasonCode.GRANTED_QOS_2
        inMessage.reasonCodes().get(2) == SubscribeAckReasonCode.GRANTED_QOS_1
        inMessage.reasonCodes().get(3) == SubscribeAckReasonCode.UNSPECIFIED_ERROR
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
        def inMessage = new SubscribeAckMqttInMessage(SubscribeAckMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Property:[$MqttMessageProperty.REASON_STRING] is already presented in message:[$MqttMessageType.PUBLISH_RELEASE]"
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
        inMessage.exception().message == "Unexpected message flags:[0b0101_0101] in message:[$MqttMessageType.PUBLISH_RELEASE]"
  }
}
