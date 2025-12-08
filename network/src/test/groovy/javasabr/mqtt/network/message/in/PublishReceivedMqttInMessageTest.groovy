package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.exception.MalformedProtocolMqttException
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode
import javasabr.rlib.common.util.BufferUtils

class PublishReceivedMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read message correctly as MQTT 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
        }
    when:
        def inMessage = new PublishReceivedMqttInMessage(PublishReceivedMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        with(inMessage) {
          reason() == null
          messageId() == testMessageId
          reasonCode() == PublishReceivedReasonCode.SUCCESS
          userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
        }
  }

  def "should read message correctly as MQTT 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.REASON_STRING, reasonString)
          it.putProperty(MqttMessageProperty.USER_PROPERTY, testUserProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.put(PublishReceivedReasonCode.QUOTA_EXCEEDED)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def inMessage = new PublishReceivedMqttInMessage(0 as byte)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        with(inMessage) {
          reason() == reasonString
          messageId() == testMessageId
          reasonCode() == PublishReceivedReasonCode.QUOTA_EXCEEDED
          userProperties() == testUserProperties
        }
    when:
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.put(PublishReceivedReasonCode.IMPLEMENTATION_SPECIFIC_ERROR)
          it.putMbi(0)
        }
        inMessage = new PublishReceivedMqttInMessage(PublishReceivedMqttInMessage.MESSAGE_FLAGS)
        result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        with(inMessage) {
          reason() == null
          messageId() == testMessageId
          reasonCode() == PublishReceivedReasonCode.IMPLEMENTATION_SPECIFIC_ERROR
          userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
        }
  }

  def "should not allow to put reason 2 times"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.REASON_STRING, "reason2")
          it.putProperty(MqttMessageProperty.REASON_STRING, "reason1")
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.put(PublishReceivedReasonCode.SUCCESS)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
    when:
        def inMessage = new PublishReceivedMqttInMessage(PublishReceivedMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        with(inMessage) {
          exception() instanceof MalformedProtocolMqttException
          exception().message == "Property:[$MqttMessageProperty.REASON_STRING] is already presented in message:[$MqttMessageType.PUBLISH_RECEIVED]"
        }
  }

  def "should not allow invalid message flags"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(testMessageId)
          it.put(PublishReceivedReasonCode.SUCCESS)
          it.putMbi(0)
        }
    when:
        def inMessage = new PublishReceivedMqttInMessage(0b0101_0101 as byte)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !result
        with(inMessage) {
          exception() instanceof MalformedProtocolMqttException
          exception().message == "Unexpected message flags:[0b0101_0101] in message:[$MqttMessageType.PUBLISH_RECEIVED]"
        }
  }
}
