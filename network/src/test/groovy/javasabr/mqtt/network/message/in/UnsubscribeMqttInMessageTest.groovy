package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.MqttProtocolErrors
import javasabr.mqtt.model.exception.MalformedProtocolMqttException
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.rlib.common.util.BufferUtils

class UnsubscribeMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read message correctly as MQTT 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putString(topicFilter)
          it.putString(topicFilter2)
        }
    when:
        def inMessage = new UnsubscribeMqttInMessage(UnsubscribeMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.messageId() == messageId
        inMessage.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
        def rawTopicFilters = inMessage.rawTopicFilters()
        rawTopicFilters.size() == 2
        rawTopicFilters.get(0).toString() == topicFilter
        rawTopicFilters.get(1).toString() == topicFilter2
  }

  def "should read message correctly as MQTT 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.USER_PROPERTY, userProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.putString(topicFilter)
          it.putString(topicFilter2)
        }
    when:
        def inMessage = new UnsubscribeMqttInMessage(UnsubscribeMqttInMessage.MESSAGE_FLAGS)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.messageId() == messageId
        inMessage.userProperties() == userProperties
        def rawTopicFilters = inMessage.rawTopicFilters()
        rawTopicFilters.size() == 2
        rawTopicFilters.get(0).toString() == topicFilter
        rawTopicFilters.get(1).toString() == topicFilter2
    when:
        def dataBuffer2 = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(0)
          it.putString(topicFilter)
          it.putString(topicFilter2)
        }
        def inMessage2 = new UnsubscribeMqttInMessage(UnsubscribeMqttInMessage.MESSAGE_FLAGS)
        def result2 = inMessage2.read(defaultMqtt5Connection, dataBuffer2, dataBuffer2.limit())
    then:
        result2
        inMessage2.messageId() == messageId
        inMessage2.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
        def rawTopicFilters2 = inMessage2.rawTopicFilters()
        rawTopicFilters2.size() == 2
        rawTopicFilters2.get(0).toString() == topicFilter
        rawTopicFilters2.get(1).toString() == topicFilter2
  }

  def "should not read invalid message as MQTT 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putString(topicFilter)
        }
    when:
        def inMessage = new UnsubscribeMqttInMessage(0b0000_0000 as byte)
        def successful = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        !successful
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Unexpected message flags:[0b0000_0000] in message:[$MqttMessageType.UNSUBSCRIBE]"
    when:
        def dataBuffer2 = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
        }
        def inMessage2 = new UnsubscribeMqttInMessage(UnsubscribeMqttInMessage.MESSAGE_FLAGS)
        def successful2 = inMessage2.read(defaultMqtt311Connection, dataBuffer2, dataBuffer2.limit())
    then:
        !successful2
        inMessage2.exception() instanceof MalformedProtocolMqttException
        inMessage2.exception().message == MqttProtocolErrors.NO_ANY_TOPIC_FILTERS
  }

  def "should not read invalid message as MQTT 5.0"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(0)
        }
    when:
        def inMessage = new UnsubscribeMqttInMessage(0b0000_0000 as byte)
        def successful = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !successful
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == "Unexpected message flags:[0b0000_0000] in message:[$MqttMessageType.UNSUBSCRIBE]"
    when:
        def dataBuffer2 = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(0)
        }
        def inMessage2 = new UnsubscribeMqttInMessage(UnsubscribeMqttInMessage.MESSAGE_FLAGS)
        def successful2 = inMessage2.read(defaultMqtt5Connection, dataBuffer2, dataBuffer2.limit())
    then:
        !successful2
        inMessage2.exception() instanceof MalformedProtocolMqttException
        inMessage2.exception().message == MqttProtocolErrors.NO_ANY_TOPIC_FILTERS
    when:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.SERVER_REFERENCE, "reference")
        }
        def dataBuffer3 = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
        }
        def inMessage3 = new UnsubscribeMqttInMessage(UnsubscribeMqttInMessage.MESSAGE_FLAGS)
        def successful3 = inMessage3.read(defaultMqtt5Connection, dataBuffer3, dataBuffer3.limit())
    then:
        !successful3
        inMessage3.exception() instanceof MalformedProtocolMqttException
        inMessage3.exception().message == "Property:[$MqttMessageProperty.SERVER_REFERENCE] is not available for message:[$MqttMessageType.UNSUBSCRIBE]"
  }
}
