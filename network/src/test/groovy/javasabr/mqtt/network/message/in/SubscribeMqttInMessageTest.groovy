package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.PacketProperty
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.SubscribeRetainHandling
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class SubscribeMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read packet correctly as mqtt 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(packetId)
          it.putString(topicFilter)
          it.put(0b0000_0001 as byte)
          it.putString(topicFilter2)
          it.put(0b0000_0010 as byte)
        }
    when:
        def packet = new SubscribeMqttInMessage(0b1000_0000 as byte)
        def result = packet.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.subscriptions.size() == 2
        packet.subscriptions.get(0).getQos() == QoS.AT_LEAST_ONCE
        packet.subscriptions.get(0).getTopicFilter().toString() == topicFilter
        packet.subscriptions.get(0).isNoLocal()
        packet.subscriptions.get(0).isRetainAsPublished()
        packet.subscriptions.get(0).getRetainHandling() == SubscribeRetainHandling.SEND
        packet.subscriptions.get(1).getQos() == QoS.EXACTLY_ONCE
        packet.subscriptions.get(1).getTopicFilter().toString() == topicFilter2
        packet.subscriptions.get(1).isNoLocal()
        packet.subscriptions.get(1).isRetainAsPublished()
        packet.subscriptions.get(1).getRetainHandling() == SubscribeRetainHandling.SEND
        packet.messageId == packetId
        packet.userProperties() == Array.empty()
        packet.subscriptionId == MqttProperties.SUBSCRIPTION_ID_UNDEFINED
  }

  def "should read packet correctly as mqtt 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(PacketProperty.SUBSCRIPTION_IDENTIFIER, subscriptionId)
          it.putProperty(PacketProperty.USER_PROPERTY, userProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(packetId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.putString(topicFilter)
          it.put(0b0000_1001 as byte)
          it.putString(topicFilter2)
          it.put(0b0001_0110 as byte)
        }
    when:
        def packet = new SubscribeMqttInMessage(0b0110_0000 as byte)
        def result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.subscriptions.size() == 2
        packet.subscriptions.get(0).getQos() == QoS.AT_LEAST_ONCE
        packet.subscriptions.get(0).getTopicFilter().toString() == topicFilter
        !packet.subscriptions.get(0).isNoLocal()
        packet.subscriptions.get(0).isRetainAsPublished()
        packet.subscriptions.get(0).getRetainHandling() == SubscribeRetainHandling.SEND
        packet.subscriptions.get(1).getQos() == QoS.EXACTLY_ONCE
        packet.subscriptions.get(1).getTopicFilter().toString() == topicFilter2
        packet.subscriptions.get(1).isNoLocal()
        !packet.subscriptions.get(1).isRetainAsPublished()
        packet.subscriptions.get(1).getRetainHandling() == SubscribeRetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST
        packet.messageId == packetId
        packet.userProperties() == userProperties
        packet.subscriptionId == subscriptionId
    when:
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(packetId)
          it.putMbi(0)
          it.putString(topicFilter)
          it.put(0b0000_0001 as byte)
          it.putString(topicFilter2)
          it.put(0b0000_0010 as byte)
        }
        packet = new SubscribeMqttInMessage(0b0110_0000 as byte)
        result = packet.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        packet.subscriptions.size() == 2
        packet.subscriptions.get(0).getQos() == QoS.AT_LEAST_ONCE
        packet.subscriptions.get(0).getTopicFilter().toString() == topicFilter
        !packet.subscriptions.get(0).isNoLocal()
        !packet.subscriptions.get(0).isRetainAsPublished()
        packet.subscriptions.get(0).getRetainHandling() == SubscribeRetainHandling.SEND
        packet.subscriptions.get(1).getQos() == QoS.EXACTLY_ONCE
        packet.subscriptions.get(1).getTopicFilter().toString() == topicFilter2
        !packet.subscriptions.get(1).isNoLocal()
        !packet.subscriptions.get(1).isRetainAsPublished()
        packet.subscriptions.get(1).getRetainHandling() == SubscribeRetainHandling.SEND
        packet.messageId == packetId
        packet.userProperties() == Array.empty()
        packet.subscriptionId == MqttProperties.SUBSCRIPTION_ID_UNDEFINED
  }
}
