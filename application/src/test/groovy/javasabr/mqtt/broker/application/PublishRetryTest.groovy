package javasabr.mqtt.broker.application

import com.hivemq.client.mqtt.datatypes.MqttQos
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode
import javasabr.mqtt.model.subscription.Subscription
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.network.message.in.ConnectAckMqttInMessage
import javasabr.mqtt.network.message.in.PublishMqttInMessage
import javasabr.mqtt.network.message.in.PublishReleaseMqttInMessage
import javasabr.mqtt.network.message.in.SubscribeAckMqttInMessage
import javasabr.mqtt.network.message.out.ConnectMqtt311OutMessage
import javasabr.mqtt.network.message.out.ConnectMqtt5OutMessage
import javasabr.mqtt.network.message.out.PublishCompleteMqtt311OutMessage
import javasabr.mqtt.network.message.out.PublishCompleteMqtt5OutMessage
import javasabr.mqtt.network.message.out.PublishReceivedMqtt311OutMessage
import javasabr.mqtt.network.message.out.PublishReceivedMqtt5OutMessage
import javasabr.mqtt.network.message.out.SubscribeMqtt311OutMessage
import javasabr.mqtt.network.message.out.SubscribeMqtt5OutMessage
import javasabr.mqtt.service.session.MqttSessionService
import javasabr.rlib.collections.array.Array
import org.springframework.beans.factory.annotation.Autowired

class PublishRetryTest extends IntegrationSpecification {

  @Autowired
  MqttSessionService mqttSessionService

  def "mqtt 3.1.1 client should be generate session with one pending QoS 1 packet"() {
    given:
        def deviceId = generateClientId("device")
        def serviceId = generateClientId("service")
        def serviceName = "PublishRetryTest1"
        def publisher = buildExternalMqtt5Client(deviceId)
        def subscriber = buildMqtt311MockClient()
    when:
        publisher.connect().join()
        subscriber.connect()
        subscriber.send(new ConnectMqtt311OutMessage(serviceId, keepAlive))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
    when:
        subscriber.send(new SubscribeMqtt311OutMessage(
            1,
            Array.of(Subscription.minimal(TopicFilter.valueOf("service/$serviceName/device/+"), QoS.AT_LEAST_ONCE))))
    then:
        with(subscriber.readNext() as SubscribeAckMqttInMessage) {
          reasonCodes()
              .stream()
              .allMatch({ it == SubscribeAckReasonCode.GRANTED_QOS_1 })
        }
    when:
        publisher
            .publishWith()
            .topic("service/$serviceName/device/$deviceId")
            .qos(MqttQos.AT_MOST_ONCE)
            .payload(publishPayload)
            .send()
            .join()
    then:
        def receivedPublish = subscriber.readNext() as PublishMqttInMessage
        with(receivedPublish) {
          payload() == publishPayload
        }
    when:
        subscriber.disconnect()
        Thread.sleep(1_000)
        subscriber.connect()
        subscriber.send(new ConnectMqtt311OutMessage(serviceId, keepAlive))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
        with(subscriber.readNext() as PublishMqttInMessage) {
          duplicate()
          messageId() == receivedPublish.messageId()
          payload() == publishPayload
        }
    cleanup:
        subscriber.close()
        publisher.disconnect().join()
  }

  def "mqtt 5 client should be generate session with one pending QoS 1 packet"() {
    given:
        def deviceId = generateClientId("device")
        def serviceId = generateClientId("service")
        def serviceName = "PublishRetryTest2"
        def publisher = buildExternalMqtt5Client(deviceId)
        def subscriber = buildMqtt5MockClient()
    when:
        publisher.connect().join()
        subscriber.connect()
        subscriber.send(new ConnectMqtt5OutMessage(serviceId, keepAlive, 120))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
    when:
        subscriber.send(new SubscribeMqtt5OutMessage(
            1,
            Array.of(Subscription.minimal(TopicFilter.valueOf("service/$serviceName/device/+"), QoS.AT_LEAST_ONCE))))
    then:
        with(subscriber.readNext() as SubscribeAckMqttInMessage) {
          reasonCodes()
              .stream()
              .allMatch({ it == SubscribeAckReasonCode.GRANTED_QOS_1 })
        }
    when:
        publisher
            .publishWith()
            .topic("service/$serviceName/device/$deviceId")
            .qos(MqttQos.AT_MOST_ONCE)
            .payload(publishPayload)
            .send()
            .join()
    then:
        def receivedPublish = subscriber.readNext() as PublishMqttInMessage
        with(receivedPublish) {
          payload() == publishPayload
        }
    when:
        subscriber.disconnect()
        subscriber.connect()
        subscriber.send(new ConnectMqtt5OutMessage(serviceId, keepAlive, 120))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
        with(subscriber.readNext() as PublishMqttInMessage) {
          duplicate()
          messageId() == receivedPublish.messageId()
          payload() == publishPayload
        }
    cleanup:
        subscriber.close()
        publisher.disconnect().join()
  }

  def "mqtt 3.1.1 client should be generate session with one pending QoS 2 packet"() {
    given:
        def deviceId = generateClientId("device")
        def serviceId = generateClientId("service")
        def serviceName = "PublishRetryTest3"
        def publisher = buildExternalMqtt5Client(deviceId)
        def subscriber = buildMqtt311MockClient()
    when:
        publisher.connect().join()
        subscriber.connect()
        subscriber.send(new ConnectMqtt311OutMessage(serviceId, keepAlive))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
    when:
        subscriber.send(new SubscribeMqtt311OutMessage(
            1,
            Array.of(Subscription.minimal(TopicFilter.valueOf("service/$serviceName/device/+"), QoS.EXACTLY_ONCE))))
    then:
        with(subscriber.readNext() as SubscribeAckMqttInMessage) {
          reasonCodes()
              .stream()
              .allMatch({ it == SubscribeAckReasonCode.GRANTED_QOS_2 })
        }
    when:
        publisher
            .publishWith()
            .topic("service/$serviceName/device/$deviceId")
            .qos(MqttQos.AT_MOST_ONCE)
            .payload(publishPayload)
            .send()
            .join()
    then:
        def receivedPublish = subscriber.readNext() as PublishMqttInMessage
        with(receivedPublish) {
          payload() == publishPayload
        }
    when:
        subscriber.disconnect()
        subscriber.connect()
        subscriber.send(new ConnectMqtt311OutMessage(serviceId, keepAlive))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
        with(subscriber.readNext() as PublishMqttInMessage) {
          duplicate()
          messageId() == receivedPublish.messageId()
          payload() == publishPayload
        }
    when:
        subscriber.disconnect()
        subscriber.connect()
        subscriber.send(new ConnectMqtt311OutMessage(serviceId, keepAlive))
        subscriber.send(new PublishReceivedMqtt311OutMessage(receivedPublish.messageId()))
        subscriber.send(new PublishCompleteMqtt311OutMessage(receivedPublish.messageId()))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
        with(subscriber.readNext() as PublishMqttInMessage) {
          duplicate()
          messageId() == receivedPublish.messageId()
          payload() == publishPayload
        }
        with(subscriber.readNext() as PublishReleaseMqttInMessage) {
          messageId() == receivedPublish.messageId()
        }
    cleanup:
        subscriber.close()
        publisher.disconnect().join()
  }

  def "mqtt 5 client should be generate session with one pending QoS 2 packet"() {
    given:
        def deviceId = generateClientId("device")
        def serviceId = generateClientId("service")
        def serviceName = "PublishRetryTest4"
        def publisher = buildExternalMqtt5Client(deviceId)
        def subscriber = buildMqtt5MockClient()
    when:
        publisher.connect().join()
        subscriber.connect()
        subscriber.send(new ConnectMqtt5OutMessage(serviceId, keepAlive, 120))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
    when:
        subscriber.send(new SubscribeMqtt5OutMessage(
            1,
            Array.of(Subscription.minimal(TopicFilter.valueOf("service/$serviceName/device/+"), QoS.EXACTLY_ONCE))))
    then:
        with(subscriber.readNext() as SubscribeAckMqttInMessage) {
          reasonCodes()
              .stream()
              .allMatch({ it == SubscribeAckReasonCode.GRANTED_QOS_2 })
        }
    when:
        publisher
            .publishWith()
            .topic("service/$serviceName/device/$deviceId")
            .qos(MqttQos.AT_MOST_ONCE)
            .payload(publishPayload)
            .send()
            .join()
    then:
        def receivedPublish = subscriber.readNext() as PublishMqttInMessage
        with(receivedPublish) {
          payload() == publishPayload
        }
    when:
        subscriber.disconnect()
        subscriber.connect()
        subscriber.send(new ConnectMqtt5OutMessage(serviceId, keepAlive, 120))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
        with(subscriber.readNext() as PublishMqttInMessage) {
          duplicate()
          messageId() == receivedPublish.messageId()
          payload() == publishPayload
        }
    when:
        subscriber.disconnect()
        subscriber.connect()
        subscriber.send(new ConnectMqtt5OutMessage(serviceId, keepAlive, 120))
        subscriber.send(new PublishReceivedMqtt5OutMessage(
            receivedPublish.messageId(),
            PublishReceivedReasonCode.SUCCESS
        ))
        subscriber.send(new PublishCompleteMqtt5OutMessage(
            receivedPublish.messageId(),
            PublishCompletedReasonCode.SUCCESS
        ))
    then:
        with(subscriber.readNext() as ConnectAckMqttInMessage) {
          reasonCode() == ConnectAckReasonCode.SUCCESS
        }
        with(subscriber.readNext() as PublishMqttInMessage) {
          duplicate()
          messageId() == receivedPublish.messageId()
          payload() == publishPayload
        }
        with(subscriber.readNext() as PublishReleaseMqttInMessage) {
          messageId() == receivedPublish.messageId()
        }
    cleanup:
        subscriber.close()
        publisher.disconnect().join()
  }
}
