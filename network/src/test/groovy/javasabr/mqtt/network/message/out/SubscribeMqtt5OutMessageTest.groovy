package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.SubscribeRetainHandling
import javasabr.mqtt.model.subscribtion.RequestedSubscription
import javasabr.mqtt.model.subscribtion.Subscription
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.network.message.in.SubscribeMqttInMessage
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils

class SubscribeMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write message correctly"() {
    given:
        def subscription1 = Subscription
            .minimal(TopicFilter.valueOf("topic/name1"), QoS.AT_LEAST_ONCE)
        def requestedSubscription1 = RequestedSubscription
            .minimal("topic/name1", QoS.AT_LEAST_ONCE)
        def subscription2 = new Subscription(
            TopicFilter.valueOf("topic/name2"),
            15,
            QoS.EXACTLY_ONCE,
            SubscribeRetainHandling.DO_NOT_SEND,
            false,
            false)
        def requestedSubscription2 = new RequestedSubscription(
            "topic/name2",
            QoS.EXACTLY_ONCE,
            SubscribeRetainHandling.DO_NOT_SEND,
            false,
            false)
        def subscriptions = Array.of(subscription1, subscription2)
        def requestedSubscriptions = Array.of(requestedSubscription1, requestedSubscription2)
        def outMessage = new SubscribeMqtt5OutMessage(
            1,
            subscriptions,
            userProperties,
            MqttProperties.SUBSCRIPTION_ID_IS_NOT_SET)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt5Connection, it)
        }
        def inMessage = new SubscribeMqttInMessage(0b0000_0010 as byte)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.messageId() == 1
        inMessage.subscriptions() == requestedSubscriptions
        inMessage.userProperties() == userProperties
        inMessage.subscriptionId() == MqttProperties.SUBSCRIPTION_ID_IS_NOT_SET
    when:
        outMessage = new SubscribeMqtt5OutMessage(
            25,
            subscriptions,
            userProperties,
            35)
        dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt5Connection, it)
        }
        inMessage = new SubscribeMqttInMessage(0b0000_0010 as byte)
        result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.messageId() == 25
        inMessage.subscriptions() == requestedSubscriptions
        inMessage.userProperties() == userProperties
        inMessage.subscriptionId() == 35
  }
}
