package javasabr.mqtt.application.network.out

import javasabr.mqtt.model.QoS
import javasabr.mqtt.network.message.in.PublishMqttInMessage
import javasabr.mqtt.network.message.out.PublishMqtt5OutMessage
import javasabr.rlib.common.util.BufferUtils

class PublishMqtt5OutMessageTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new PublishMqtt5OutMessage(
            packetId,
            QoS.EXACTLY_ONCE,
            true,
            true,
            publishTopic.toString(),
            publishPayload,
            topicAlias,
            false,
            responseTopic,
            correlationData,
            userProperties)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt5Connection, it)
        }
        def reader = new PublishMqttInMessage(0b0011_1101 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.messageId == packetId
        reader.qos == QoS.EXACTLY_ONCE
        reader.retained
        reader.duplicate
        reader.payload == publishPayload
        reader.topicName == publishTopic
        reader.userProperties() == userProperties
        reader.topicAlias == topicAlias
        !reader.payloadFormatIndicator
        reader.responseTopic == responseTopic
        reader.correlationData == correlationData
    when:
        packet = new PublishMqtt5OutMessage(
            packetId,
            QoS.AT_MOST_ONCE,
            false,
            false,
            publishTopic.toString(),
            publishPayload,
            topicAlias,
            true,
            responseTopic,
            correlationData,
            userProperties)

        dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt5Connection, it)
        }

        reader = new PublishMqttInMessage(0b0011_0000 as byte)
        result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.messageId == 0
        reader.qos == QoS.AT_MOST_ONCE
        !reader.retained
        !reader.duplicate
        reader.payload == publishPayload
        reader.topicName == publishTopic
        reader.userProperties() == userProperties
        reader.topicAlias == topicAlias
        reader.payloadFormatIndicator
        reader.responseTopic == responseTopic
        reader.correlationData == correlationData
  }
}
