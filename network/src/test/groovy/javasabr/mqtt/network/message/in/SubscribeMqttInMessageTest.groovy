package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.MqttMessageProperty
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.SubscribeRetainHandling
import javasabr.rlib.common.util.BufferUtils

class SubscribeMqttInMessageTest extends BaseMqttInMessageTest {

  def "should read message correctly as mqtt 3.1.1"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putString(topicFilter)
          it.put(0b0000_0001 as byte) // QoS.AT_LEAST_ONCE
          it.putString(topicFilter2)
          it.put(0b0000_0010 as byte) // QoS.EXACTLY_ONCE
        }
    when:
        def inMessage = new SubscribeMqttInMessage(0b0000_0010 as byte)
        def successful = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        successful
        def subscriptions = inMessage.subscriptions()
        subscriptions.size() == 2
        subscriptions.get(0).qos() == QoS.AT_LEAST_ONCE
        subscriptions.get(0).rawTopicFilter() == topicFilter
        subscriptions.get(0).noLocal()
        subscriptions.get(0).retainAsPublished()
        subscriptions.get(0).retainHandling() == SubscribeRetainHandling.SEND
        subscriptions.get(1).qos() == QoS.EXACTLY_ONCE
        subscriptions.get(1).rawTopicFilter().toString() == topicFilter2
        subscriptions.get(1).noLocal()
        subscriptions.get(1).retainAsPublished()
        subscriptions.get(1).retainHandling() == SubscribeRetainHandling.SEND
        inMessage.messageId() == messageId
        inMessage.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
        inMessage.subscriptionId() == MqttProperties.SUBSCRIPTION_ID_IS_NOT_SET
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
          it.put(0b0000_1001 as byte)  // QoS.AT_LEAST_ONCE
          it.putString(topicFilter2)
          it.put(0b0001_0110 as byte)  // QoS.AT_LEAST_ONCE
        }
    when:
        def inMessage = new SubscribeMqttInMessage(0b0000_0010 as byte)
        def successful = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
        def subscriptions = inMessage.subscriptions()
    then:
        successful
        subscriptions.size() == 2
        subscriptions.get(0).qos() == QoS.AT_LEAST_ONCE
        subscriptions.get(0).rawTopicFilter() == topicFilter
        !subscriptions.get(0).noLocal()
        subscriptions.get(0).retainAsPublished()
        subscriptions.get(0).retainHandling() == SubscribeRetainHandling.SEND
        subscriptions.get(1).qos() == QoS.EXACTLY_ONCE
        subscriptions.get(1).rawTopicFilter() == topicFilter2
        subscriptions.get(1).noLocal()
        !subscriptions.get(1).retainAsPublished()
        subscriptions.get(1).retainHandling() == SubscribeRetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST
        inMessage.messageId() == messageId
        inMessage.userProperties() == userProperties
        inMessage.subscriptionId() == subscriptionId
    when:
        dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(0)
          it.putString(topicFilter)
          it.put(0b0000_0001 as byte)
          it.putString(topicFilter2)
          it.put(0b0000_0010 as byte)
        }
        inMessage = new SubscribeMqttInMessage(0b0000_0010 as byte)
        successful = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
        subscriptions = inMessage.subscriptions()
    then:
        successful
        subscriptions.size() == 2
        subscriptions.get(0).qos() == QoS.AT_LEAST_ONCE
        subscriptions.get(0).rawTopicFilter() == topicFilter
        !subscriptions.get(0).noLocal()
        !subscriptions.get(0).retainAsPublished()
        subscriptions.get(0).retainHandling() == SubscribeRetainHandling.SEND
        subscriptions.get(1).qos() == QoS.EXACTLY_ONCE
        subscriptions.get(1).rawTopicFilter() == topicFilter2
        !subscriptions.get(1).noLocal()
        !subscriptions.get(1).retainAsPublished()
        subscriptions.get(1).retainHandling() == SubscribeRetainHandling.SEND
        inMessage.messageId() == messageId
        inMessage.userProperties() == MqttInMessage.EMPTY_USER_PROPERTIES
        inMessage.subscriptionId() == MqttProperties.SUBSCRIPTION_ID_IS_NOT_SET
  }
}
