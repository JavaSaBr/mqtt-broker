package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.exception.MalformedProtocolMqttException
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode
import javasabr.rlib.common.util.BufferUtils

class SubscribeAckMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read message correctly as MQTT 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.put(SubscribeAckReasonCode.GRANTED_QOS_0)
          it.put(SubscribeAckReasonCode.GRANTED_QOS_2)
          it.put(SubscribeAckReasonCode.GRANTED_QOS_1)
          it.put(SubscribeAckReasonCode.UNSPECIFIED_ERROR)
        }
    when:
        def inMessage = new SubscribeAckMqttInMessage(SubscribeAckMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.reason() == null
        inMessage.messageId() == messageId
        def reasonCodes = inMessage.reasonCodes()
        reasonCodes.size() == 4
        reasonCodes.get(0) == SubscribeAckReasonCode.GRANTED_QOS_0
        reasonCodes.get(1) == SubscribeAckReasonCode.GRANTED_QOS_2
        reasonCodes.get(2) == SubscribeAckReasonCode.GRANTED_QOS_1
        reasonCodes.get(3) == SubscribeAckReasonCode.UNSPECIFIED_ERROR
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
          it.put(SubscribeAckReasonCode.GRANTED_QOS_0)
          it.put(SubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR)
          it.put(SubscribeAckReasonCode.GRANTED_QOS_1)
          it.put(SubscribeAckReasonCode.UNSPECIFIED_ERROR)
        }
    when:
        def inMessage = new SubscribeAckMqttInMessage(SubscribeAckMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.reason() == reasonString
        inMessage.messageId() == messageId
        inMessage.userProperties() == userProperties
        def reasonCodes = inMessage.reasonCodes()
        reasonCodes.size() == 4
        reasonCodes.get(0) == SubscribeAckReasonCode.GRANTED_QOS_0
        reasonCodes.get(1) == SubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR
        reasonCodes.get(2) == SubscribeAckReasonCode.GRANTED_QOS_1
        reasonCodes.get(3) == SubscribeAckReasonCode.UNSPECIFIED_ERROR
    when:
        def dataBuffer2 = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(0)
          it.putByte(SubscribeAckReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED.code())
          it.putByte(SubscribeAckReasonCode.GRANTED_QOS_2.code())
          it.putByte(SubscribeAckReasonCode.GRANTED_QOS_1.code())
          it.putByte(SubscribeAckReasonCode.UNSPECIFIED_ERROR.code())
        }
        def inMessage2 = new SubscribeAckMqttInMessage(SubscribeAckMqttInMessage.MESSAGE_FLAGS)
        def result2 = inMessage2.read(defaultMqtt5Connection, dataBuffer2, dataBuffer2.limit())
    then:
        result2
        inMessage2.reason() == null
        inMessage2.messageId() == messageId
        inMessage2.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
        def reasonCodes2 = inMessage2.reasonCodes()
        reasonCodes2.size() == 4
        reasonCodes2.get(0) == SubscribeAckReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED
        reasonCodes2.get(1) == SubscribeAckReasonCode.GRANTED_QOS_2
        reasonCodes2.get(2) == SubscribeAckReasonCode.GRANTED_QOS_1
        reasonCodes2.get(3) == SubscribeAckReasonCode.UNSPECIFIED_ERROR
  }

  def "should not allow to put reason 2 times"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.REASON_STRING, "reason2")
          it.putProperty(MqttMessageProperty.REASON_STRING, "reason1")
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.put(SubscribeAckReasonCode.GRANTED_QOS_0)
          it.put(SubscribeAckReasonCode.GRANTED_QOS_1)
        }
    when:
        def inMessage = new SubscribeAckMqttInMessage(SubscribeAckMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Property:[$MqttMessageProperty.REASON_STRING] is already presented in message:[$MqttMessageType.SUBSCRIBE_ACK]"
  }

  def "should not allow invalid message flags"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(0)
          it.put(SubscribeAckReasonCode.GRANTED_QOS_0)
          it.put(SubscribeAckReasonCode.GRANTED_QOS_1)
        }
    when:
        def inMessage = new SubscribeAckMqttInMessage(0b0101_0101 as byte)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Unexpected message flags:[0b0101_0101] in message:[$MqttMessageType.SUBSCRIBE_ACK]"
  }
}
