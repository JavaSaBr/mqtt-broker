package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.data.type.StringPair
import javasabr.mqtt.model.subscribtion.RequestedSubscription
import javasabr.mqtt.model.subscribtion.Subscription
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.network.message.in.SubscribeMqttInMessage
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class SubscribeMqtt311OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def subscription1 = Subscription
            .minimal(TopicFilter.valueOf("topic/name1"), QoS.AT_LEAST_ONCE)
        def requestedSubscription1 = RequestedSubscription
            .minimal("topic/name1", QoS.AT_LEAST_ONCE)
        def subscription2 = Subscription
            .minimal(TopicFilter.valueOf("topic/name2"), QoS.EXACTLY_ONCE)
        def requestedSubscription2 = RequestedSubscription
            .minimal("topic/name2", QoS.EXACTLY_ONCE,)
        def subscriptions = Array.of(subscription1, subscription2)
        def requestedSubscriptions = Array.of(requestedSubscription1, requestedSubscription2)
        def outMessage = new SubscribeMqtt311OutMessage(1, subscriptions)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt311Connection, it)
        }
        def inMessage = new SubscribeMqttInMessage(0b1000_0000 as byte)
        def result = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.messageId() == 1
        inMessage.subscriptions() == requestedSubscriptions
        inMessage.userProperties() == Array.empty(StringPair)
        inMessage.subscriptionId() == MqttProperties.SUBSCRIPTION_ID_UNDEFINED
  }
}
