package javasabr.mqtt.network.message.out

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.network.message.in.SubscribeMqttInMessage
import javasabr.rlib.common.util.BufferUtils

class SubscribeMqtt5OutMessageTest extends BaseMqttOutMessageTest {

  def "should write packet correctly"() {
    given:
        def packet = new SubscribeMqtt5OutMessage(
            topicFiltersObj5,
            1,
            userProperties,
            MqttProperties.SUBSCRIPTION_ID_UNDEFINED)
    when:
        def dataBuffer = BufferUtils.prepareBuffer(512) {
          packet.write(defaultMqtt5Connection, it)
        }
        def reader = new SubscribeMqttInMessage(0b1000_0000 as byte)
        def result = reader.read(defaultMqtt5Connection, dataBuffer, dataBuffer.limit())
    then:
        result
        reader.messageId == 1
        reader.subscriptions == topicFiltersObj5
        reader.userProperties() == userProperties
        reader.subscriptionId == MqttProperties.SUBSCRIPTION_ID_UNDEFINED
  }
}
