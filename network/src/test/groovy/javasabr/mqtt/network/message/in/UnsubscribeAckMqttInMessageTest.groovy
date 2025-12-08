package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.exception.MalformedProtocolMqttException
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode
import javasabr.rlib.common.util.BufferUtils

class UnsubscribeAckMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read message correctly as MQTT 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
        }
    when:
        def inMessage = new UnsubscribeAckMqttInMessage(UnsubscribeAckMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.reason() == null
        inMessage.messageId() == testMessageId
        inMessage.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
  }

  def "should read message correctly as MQTT 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.REASON_STRING, reasonString)
          it.putProperty(MqttMessageProperty.USER_PROPERTY, testUserProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.put(UnsubscribeAckReasonCode.SUCCESS)
          it.put(UnsubscribeAckReasonCode.SUCCESS)
          it.put(UnsubscribeAckReasonCode.NOT_AUTHORIZED)
          it.put(UnsubscribeAckReasonCode.UNSPECIFIED_ERROR)
        }
    when:
        def inMessage = new UnsubscribeAckMqttInMessage(UnsubscribeAckMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.reason() == reasonString
        inMessage.messageId() == testMessageId
        inMessage.userProperties() == testUserProperties
        def reasonCodes = inMessage.reasonCodes()
        reasonCodes.size() == 4
        reasonCodes.get(0) == UnsubscribeAckReasonCode.SUCCESS
        reasonCodes.get(1) == UnsubscribeAckReasonCode.SUCCESS
        reasonCodes.get(2) == UnsubscribeAckReasonCode.NOT_AUTHORIZED
        reasonCodes.get(3) == UnsubscribeAckReasonCode.UNSPECIFIED_ERROR
    when:
        def dataBuffer2 = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.putMbi(0)
          it.put(UnsubscribeAckReasonCode.UNSPECIFIED_ERROR)
          it.put(UnsubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR)
        }
        def inMessage2 = new UnsubscribeAckMqttInMessage(UnsubscribeAckMqttInMessage.MESSAGE_FLAGS)
        def result2 = inMessage2.read(defaultMqtt5Connection, dataBuffer2, dataBuffer2.limit())
    then:
        result2
        inMessage2.reason() == null
        inMessage2.messageId() == testMessageId
        inMessage2.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
        def reasonCodes2 = inMessage2.reasonCodes()
        reasonCodes2.size() == 2
        reasonCodes2.get(0) == UnsubscribeAckReasonCode.UNSPECIFIED_ERROR
        reasonCodes2.get(1) == UnsubscribeAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR
  }

  def "should not allow to put reason 2 times"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.REASON_STRING, "reason2")
          it.putProperty(MqttMessageProperty.REASON_STRING, "reason1")
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.put(UnsubscribeAckReasonCode.SUCCESS)
          it.put(UnsubscribeAckReasonCode.SUCCESS)
        }
    when:
        def inMessage = new UnsubscribeAckMqttInMessage(UnsubscribeAckMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Property:[$MqttMessageProperty.REASON_STRING] is already presented in message:[$MqttMessageType.UNSUBSCRIBE_ACK]"
  }

  def "should not allow invalid message flags"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.putMbi(0)
          it.put(UnsubscribeAckReasonCode.SUCCESS)
          it.put(UnsubscribeAckReasonCode.SUCCESS)
        }
    when:
        def inMessage = new UnsubscribeAckMqttInMessage(0b0101_0101 as byte)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Unexpected message flags:[0b0101_0101] in message:[$MqttMessageType.UNSUBSCRIBE_ACK]"
  }
}
