package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.subscription.RequestedSubscription
import javasabr.mqtt.model.subscription.Subscription
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.network.message.in.SubscribeMqttInMessage
import javasabr.rlib.collections.array.Array
import javasabr.rlib.common.util.BufferUtils
import javasabr.rlib.common.util.NumberUtils

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
        def typeAndFlags = outMessage.messageTypeAndFlags()
        byte type = NumberUtils.getHighByteBits(typeAndFlags);
        byte info = NumberUtils.getLowByteBits(typeAndFlags);
    then:
        MqttMessageType.fromByte(type) == MqttMessageType.SUBSCRIBE
        info == SubscribeMqttInMessage.MESSAGE_FLAGS
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          outMessage.write(defaultMqtt311Connection, it)
        }
        def inMessage = new SubscribeMqttInMessage(info)
        def result = inMessage.read(defaultMqtt311Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        inMessage.messageId() == 1
        inMessage.subscriptions() == requestedSubscriptions
        inMessage.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        inMessage.subscriptionId() == MqttProperties.SUBSCRIPTION_ID_IS_NOT_SET
  }
}
