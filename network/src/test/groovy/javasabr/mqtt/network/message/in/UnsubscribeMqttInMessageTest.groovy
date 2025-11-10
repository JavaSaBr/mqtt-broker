package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.rlib.common.util.BufferUtils

class UnsubscribeMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read packet correctly as mqtt 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putString(topicFilter)
          it.putString(topicFilter2)
        }
    when:
        def packet = new UnsubscribeMqttInMessage(0b0000_0010 as byte)
        def result = packet.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.rawTopicFilters().size() == 2
        packet.rawTopicFilters().get(0).toString() == topicFilter
        packet.rawTopicFilters().get(1).toString() == topicFilter2
        packet.messageId == messageId
        packet.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
  }

  def "should read packet correctly as mqtt 5.0"() {
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
        def packet = new UnsubscribeMqttInMessage(0b0000_0010 as byte)
        def result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.rawTopicFilters().size() == 2
        packet.rawTopicFilters().get(0).toString() == topicFilter
        packet.rawTopicFilters().get(1).toString() == topicFilter2
        packet.messageId == messageId
        packet.userProperties() == userProperties
    when:
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(0)
          it.putString(topicFilter)
          it.putString(topicFilter2)
        }
        packet = new UnsubscribeMqttInMessage(0b0000_0010 as byte)
        result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.rawTopicFilters.size() == 2
        packet.rawTopicFilters.get(0).toString() == topicFilter
        packet.rawTopicFilters.get(1).toString() == topicFilter2
        packet.messageId == messageId
        packet.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
  }
}
