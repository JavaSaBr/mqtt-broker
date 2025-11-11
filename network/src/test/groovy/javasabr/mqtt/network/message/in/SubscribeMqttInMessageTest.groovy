package javasabr.mqtt.network.message.in

import javasabr.mqtt.model.*
import javasabr.mqtt.model.exception.MalformedProtocolMqttException
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
          it.put(0b0000_1001 as byte)  // QoS.AT_LEAST_ONCE, with local, retainAsPublished, SubscribeRetainHandling.SEND
          it.putString(topicFilter2)
          it.put(0b0001_0110 as byte)  // QoS.EXACTLY_ONCE, no local, no retainAsPublished, SubscribeRetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST
          it.putString(topicFilter3)
          it.put(0b0010_0100 as byte)  // QoS.AT_MOST_ONCE, no local, retainAsPublished, SubscribeRetainHandling.DO_NOT_SEND
        }
    when:
        def inMessage = new SubscribeMqttInMessage(0b0000_0010 as byte)
        def successful = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
        def subscriptions = inMessage.subscriptions()
    then:
        successful
        subscriptions.size() == 3
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
        subscriptions.get(2).qos() == QoS.AT_MOST_ONCE
        subscriptions.get(2).rawTopicFilter() == topicFilter3
        subscriptions.get(2).noLocal()
        !subscriptions.get(2).retainAsPublished()
        subscriptions.get(2).retainHandling() == SubscribeRetainHandling.DO_NOT_SEND
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

  def "should not read invalid message as mqtt 5.0"() {
    given:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(0)
          it.putString(topicFilter)
          it.put(0b0000_0001 as byte)
        }
    when:
        def inMessage = new SubscribeMqttInMessage(0b0000_0000 as byte)
        def successful = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        !successful
        inMessage.exception() instanceof MalformedProtocolMqttException
        inMessage.exception().message == 'Unexpected flags bits:0b0000_0000'
    when:
        def dataBuffer2 = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(0)
          it.putString(topicFilter)
          it.put(0b0011_1001 as byte)
        }
        def inMessage2 = new SubscribeMqttInMessage(0b0000_0010 as byte)
        def successful2 = inMessage2.read(defaultMqtt5Connection, dataBuffer2, dataBuffer2.limit())
    then:
        !successful2
        inMessage2.exception() instanceof MalformedProtocolMqttException
        inMessage2.exception().message == MqttProtocolErrors.UNSUPPORTED_QOS_OR_RETAIN_HANDLING
    when:
        def dataBuffer3 = BufferUtils.prepareBuffer(512) {
          it.putShort(messageId)
          it.putMbi(0)
        }
        def inMessage3 = new SubscribeMqttInMessage(0b0000_0010 as byte)
        def successful3 = inMessage3.read(defaultMqtt5Connection, dataBuffer3, dataBuffer3.limit())
    then:
        !successful3
        inMessage3.exception() instanceof MalformedProtocolMqttException
        inMessage3.exception().message == MqttProtocolErrors.NO_ANY_TOPIC_FILTER
  }
}
