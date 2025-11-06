package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.SubscribeRetainHandling
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class SubscribeMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read message correctly as mqtt 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putString(topicFilter)
          it.put(0b0000_0001 as byte)
          it.putString(topicFilter2)
          it.put(0b0000_0010 as byte)
        }
    when:
        def message = new SubscribeMqttInMessage(0b1000_0000 as byte)
        def result = message.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        message.subscriptions().size() == 2
        message.subscriptions().get(0).qos() == QoS.AT_LEAST_ONCE
        message.subscriptions().get(0).rawTopicFilter() == topicFilter
        message.subscriptions().get(0).noLocal()
        message.subscriptions().get(0).retainAsPublished()
        message.subscriptions().get(0).retainHandling() == SubscribeRetainHandling.SEND
        message.subscriptions().get(1).qos() == QoS.EXACTLY_ONCE
        message.subscriptions().get(1).rawTopicFilter().toString() == topicFilter2
        message.subscriptions().get(1).noLocal()
        message.subscriptions().get(1).retainAsPublished()
        message.subscriptions().get(1).retainHandling() == SubscribeRetainHandling.SEND
        message.messageId() == messageId
        message.userProperties() == Array.empty()
        message.subscriptionId() == MqttProperties.SUBSCRIPTION_ID_UNDEFINED
  }

  def "should read message correctly as mqtt 5.0"() {
    given:
        def propertiesBuffer = BufferUtils.prepareBuffer(512) {
          it.putProperty(MqttMessageProperty.SUBSCRIPTION_IDENTIFIER, subscriptionId)
          it.putProperty(MqttMessageProperty.USER_PROPERTY, userProperties)
        }
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(propertiesBuffer.limit())
          it.put(propertiesBuffer)
          it.putString(topicFilter)
          it.put(0b0000_1001 as byte)
          it.putString(topicFilter2)
          it.put(0b0001_0110 as byte)
        }
    when:
        def message = new SubscribeMqttInMessage(0b0110_0000 as byte)
        def result = message.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        message.subscriptions().size() == 2
        message.subscriptions().get(0).qos() == QoS.AT_LEAST_ONCE
        message.subscriptions().get(0).rawTopicFilter().toString() == topicFilter
        !message.subscriptions().get(0).noLocal()
        message.subscriptions().get(0).retainAsPublished()
        message.subscriptions().get(0).retainHandling() == SubscribeRetainHandling.SEND
        message.subscriptions().get(1).qos() == QoS.EXACTLY_ONCE
        message.subscriptions().get(1).rawTopicFilter().toString() == topicFilter2
        message.subscriptions().get(1).noLocal()
        !message.subscriptions().get(1).retainAsPublished()
        message.subscriptions().get(1).retainHandling() == SubscribeRetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST
        message.messageId() == messageId
        message.userProperties() == userProperties
        message.subscriptionId() == subscriptionId
    when:
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(0)
          it.putString(topicFilter)
          it.put(0b0000_0001 as byte)
          it.putString(topicFilter2)
          it.put(0b0000_0010 as byte)
        }
        message = new SubscribeMqttInMessage(0b0110_0000 as byte)
        result = message.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        message.subscriptions().size() == 2
        message.subscriptions().get(0).qos() == QoS.AT_LEAST_ONCE
        message.subscriptions().get(0).rawTopicFilter().toString() == topicFilter
        !message.subscriptions().get(0).noLocal()
        !message.subscriptions().get(0).retainAsPublished()
        message.subscriptions().get(0).retainHandling() == SubscribeRetainHandling.SEND
        message.subscriptions().get(1).qos() == QoS.EXACTLY_ONCE
        message.subscriptions().get(1).rawTopicFilter().toString() == topicFilter2
        !message.subscriptions().get(1).noLocal()
        !message.subscriptions().get(1).retainAsPublished()
        message.subscriptions().get(1).retainHandling() == SubscribeRetainHandling.SEND
        message.messageId() == messageId
        message.userProperties() == Array.empty()
        message.subscriptionId() == MqttProperties.SUBSCRIPTION_ID_UNDEFINED
  }
}
