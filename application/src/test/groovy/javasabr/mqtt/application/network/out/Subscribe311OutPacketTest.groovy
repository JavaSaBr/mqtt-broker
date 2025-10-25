package javasabr.mqtt.application.network.out

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.data.type.StringPair
import javasabr.mqtt.network.message.in.SubscribeMqttInMessage
import javasabr.mqtt.network.packet.out.Subscribe311OutPacket
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class Subscribe311OutPacketTest extends BaseOutPacketTest {

  def "should write packet correctly"() {
    given:
        def packet = new Subscribe311OutPacket(
            topicFiltersObj311,
            1)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt311Connection, it)
        }
        def reader = new SubscribeMqttInMessage(0b1000_0000 as byte)
        def result = reader.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.messageId == 1
        reader.topicFilters == topicFiltersObj311
        reader.userProperties() == Array.empty(StringPair)
        reader.subscriptionId == MqttProperties.SUBSCRIPTION_ID_UNDEFINED
  }
}
