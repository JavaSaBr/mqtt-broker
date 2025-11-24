package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.SubscribeRetainHandling
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.subscription.RequestedSubscription
import javasabr.mqtt.model.subscription.Subscription
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.network.message.in.SubscribeAckMqttInMessage
import javasabr.mqtt.network.message.in.SubscribeMqttInMessage
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils
import javasabr.rlib.common.util.NumberUtils

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
        def typeAndFlags = outMessage.messageTypeAndFlags()
        byte type = NumberUtils.getHighByteBits(typeAndFlags);
        byte info = NumberUtils.getLowByteBits(typeAndFlags);
    then:
        MqttMessageType.fromByte(type) == MqttMessageType.SUBSCRIBE
        info == SubscribeMqttInMessage.MESSAGE_FLAGS
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt5Connection, it)
        }
        def inMessage = new SubscribeMqttInMessage(info)
        def result = inMessage.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.messageId() == 1
        inMessage.subscriptions() == requestedSubscriptions
        inMessage.userProperties() == userProperties
        inMessage.subscriptionId() == MqttProperties.SUBSCRIPTION_ID_IS_NOT_SET
    when:
        def outMessage2 = new SubscribeMqtt5OutMessage(
            25,
            subscriptions,
            userProperties,
            35)
        def dataBuffer2 = BufferUtils.prepareBuffer(512) {
          outMessage2.write(defaultMqtt5Connection, it)
        }
        def inMessage2 = new SubscribeMqttInMessage(0b0000_0010 as byte)
        def result2 = inMessage2.read(defaultMqtt5Connection, dataBuffer2, dataBuffer2.limit())
    then:
        result2
        inMessage2.messageId() == 25
        inMessage2.subscriptions() == requestedSubscriptions
        inMessage2.userProperties() == userProperties
        inMessage2.subscriptionId() == 35
  }
}
