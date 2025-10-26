package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.QoS
import javasabr.mqtt.network.message.in.PublishMqttInMessage
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class PublishMqtt311OutMessageTest extends BaseMqttOutMessageTest {

  def "should write packet correctly"() {
    given:
        def packet = new PublishMqtt311OutMessage(
            packetId,
            QoS.EXACTLY_ONCE,
            true,
            true,
            publishTopic.toString(),
            publishPayload)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt311Connection, it)
        }
        def reader = new PublishMqttInMessage(0b0011_1101 as byte)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.messageId == packetId
        reader.qos == QoS.EXACTLY_ONCE
        reader.retained
        reader.duplicate
        reader.payload == publishPayload
        reader.topicName == publishTopic
        reader.userProperties() == Array.empty()
    when:
        packet = new PublishMqtt311OutMessage(
            packetId,
            QoS.AT_MOST_ONCE,
            false,
            false,
            publishTopic.toString(),
            publishPayload)
        dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt311Connection, it)
        }
        reader = new PublishMqttInMessage(0b0011_0000 as byte)
        result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.messageId == 0
        reader.qos == QoS.AT_MOST_ONCE
        !reader.retained
        !reader.duplicate
        reader.payload == publishPayload
        reader.topicName == publishTopic
        reader.userProperties() == Array.empty()
  }
}
