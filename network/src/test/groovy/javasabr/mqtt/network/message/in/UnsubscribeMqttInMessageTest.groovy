package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class UnsubscribeMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read packet correctly as mqtt 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(packetId)
          it.putString(topicFilter)
          it.putString(topicFilter2)
        }
    when:
        def packet = new UnsubscribeMqttInMessage(0b1011_0000 as byte)
        def result = packet.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.rawTopicFilters.size() == 2
        packet.rawTopicFilters.get(0).toString() == topicFilter
        packet.rawTopicFilters.get(1).toString() == topicFilter2
        packet.messageId == packetId
        packet.userProperties() == Array.empty()
  }

  def "should read packet correctly as mqtt 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.USER_PROPERTY, userProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(packetId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.putString(topicFilter)
          it.putString(topicFilter2)
        }
    when:
        def packet = new UnsubscribeMqttInMessage(0b1011_0000 as byte)
        def result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.rawTopicFilters.size() == 2
        packet.rawTopicFilters.get(0).toString() == topicFilter
        packet.rawTopicFilters.get(1).toString() == topicFilter2
        packet.messageId == packetId
        packet.userProperties() == userProperties
    when:
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(packetId)
          it.putMbi(0)
          it.putString(topicFilter)
          it.putString(topicFilter2)
        }
        packet = new UnsubscribeMqttInMessage(0b1011_0000 as byte)
        result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.rawTopicFilters.size() == 2
        packet.rawTopicFilters.get(0).toString() == topicFilter
        packet.rawTopicFilters.get(1).toString() == topicFilter2
        packet.messageId == packetId
        packet.userProperties() == Array.empty()
  }
}
