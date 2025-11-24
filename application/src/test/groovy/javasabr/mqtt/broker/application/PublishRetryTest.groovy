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
        def publisher = buildExternalMqtt5Client()
        def subscriber = buildMqtt311MockClient()
        def subscriberId = generateClientId()
    when:
        publisher.connect().join()
        subscriber.connect()
        subscriber.send(new ConnectMqtt311OutMessage(subscriberId, keepAlive))
        def connectAck = subscriber.readNext() as ConnectAckMqttInMessage
    then:
        connectAck.reasonCode() == ConnectAckReasonCode.SUCCESS
    when:
        subscriber.send(new SubscribeMqtt311OutMessage(
            1,
            Array.of(Subscription.minimal(TopicFilter.valueOf("test/retry/$subscriberId"), QoS.AT_LEAST_ONCE))))
        def subscribeAck = subscriber.readNext() as SubscribeAckMqttInMessage
    then:
        subscribeAck.reasonCodes()
            .stream()
            .allMatch({ it == SubscribeAckReasonCode.GRANTED_QOS_1 })
    when:
        publisher.publishWith()
            .topic("test/retry/$subscriberId")
            .qos(MqttQos.AT_MOST_ONCE)
            .payload(publishPayload)
            .send()
            .join()
        def receivedPublish = subscriber.readNext() as PublishMqttInMessage
    then:
        receivedPublish.payload() == publishPayload
    when:
        subscriber.disconnect()
        Thread.sleep(1_000)
        subscriber.connect()
        subscriber.send(new ConnectMqtt311OutMessage(subscriberId, keepAlive))
        connectAck = subscriber.readNext() as ConnectAckMqttInMessage
        def receivedDupPublish = subscriber.readNext() as PublishMqttInMessage
    then:
        connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
        receivedDupPublish.duplicate
        receivedDupPublish.messageId == receivedPublish.messageId
        receivedDupPublish.payload == publishPayload
    cleanup:
        subscriber.close()
        publisher.disconnect().join()
  }

  def "mqtt 5 client should be generate session with one pending QoS 1 packet"() {
    given:
        def publisher = buildExternalMqtt5Client()
        def subscriber = buildMqtt5MockClient()
        def subscriberId = generateClientId()
    when:
        publisher.connect().join()
        subscriber.connect()
        subscriber.send(new ConnectMqtt5OutMessage(subscriberId, keepAlive))
        def connectAck = subscriber.readNext() as ConnectAckMqttInMessage
    then:
        connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
    when:
        subscriber.send(new SubscribeMqtt5OutMessage(
            1,
            Array.of(Subscription.minimal(TopicFilter.valueOf("test/retry/$subscriberId"), QoS.AT_LEAST_ONCE))))
        def subscribeAck = subscriber.readNext() as SubscribeAckMqttInMessage
    then:
        subscribeAck.reasonCodes.stream()
            .allMatch({ it == SubscribeAckReasonCode.GRANTED_QOS_1 })
    when:
        publisher.publishWith()
            .topic("test/retry/$subscriberId")
            .qos(MqttQos.AT_MOST_ONCE)
            .payload(publishPayload)
            .send()
            .join()

        def receivedPublish = subscriber.readNext() as PublishMqttInMessage
    then:
        receivedPublish.payload == publishPayload
    when:
        subscriber.disconnect()
        subscriber.connect()
        subscriber.send(new ConnectMqtt5OutMessage(subscriberId, keepAlive))
        connectAck = subscriber.readNext() as ConnectAckMqttInMessage
        def receivedDupPublish = subscriber.readNext() as PublishMqttInMessage
    then:
        connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
        receivedDupPublish.duplicate
        receivedDupPublish.messageId == receivedPublish.messageId
        receivedDupPublish.payload == publishPayload
    cleanup:
        subscriber.close()
        publisher.disconnect().join()
  }

  def "mqtt 3.1.1 client should be generate session with one pending QoS 2 packet"() {
    given:
        def publisher = buildExternalMqtt5Client()
        def subscriber = buildMqtt311MockClient()
        def subscriberId = generateClientId()
    when:
        publisher.connect().join()
        subscriber.connect()
        subscriber.send(new ConnectMqtt311OutMessage(subscriberId, keepAlive))
        def connectAck = subscriber.readNext() as ConnectAckMqttInMessage
    then:
        connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
    when:
        subscriber.send(new SubscribeMqtt311OutMessage(
            1,
            Array.of(Subscription.minimal(TopicFilter.valueOf("test/retry/$subscriberId"), QoS.EXACTLY_ONCE))))
        def subscribeAck = subscriber.readNext() as SubscribeAckMqttInMessage
    then:
        subscribeAck.reasonCodes.stream()
            .allMatch({ it == SubscribeAckReasonCode.GRANTED_QOS_2 })
    when:
        publisher.publishWith()
            .topic("test/retry/$subscriberId")
            .qos(MqttQos.AT_MOST_ONCE)
            .payload(publishPayload)
            .send()
            .join()

        def receivedPublish = subscriber.readNext() as PublishMqttInMessage
    then:
        receivedPublish.payload == publishPayload
    when:
        subscriber.disconnect()
        subscriber.connect()
        subscriber.send(new ConnectMqtt311OutMessage(subscriberId, keepAlive))
        connectAck = subscriber.readNext() as ConnectAckMqttInMessage
        def receivedDupPublish = subscriber.readNext() as PublishMqttInMessage
    then:
        connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
        receivedDupPublish.duplicate
        receivedDupPublish.messageId == receivedPublish.messageId
        receivedDupPublish.payload == publishPayload
    when:
        subscriber.disconnect()
        subscriber.connect()
        subscriber.send(new ConnectMqtt311OutMessage(subscriberId, keepAlive))
        connectAck = subscriber.readNext() as ConnectAckMqttInMessage
        receivedDupPublish = subscriber.readNext() as PublishMqttInMessage
        subscriber.send(new PublishReceivedMqtt311OutMessage(receivedDupPublish.messageId()))
        def releaseAck = subscriber.readNext() as PublishReleaseMqttInMessage
        subscriber.send(new PublishCompleteMqtt311OutMessage(receivedDupPublish.messageId()))
    then:
        connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
        receivedDupPublish.duplicate
        receivedDupPublish.messageId == receivedPublish.messageId
        receivedDupPublish.payload == publishPayload
        releaseAck.messageId == receivedPublish.messageId
    cleanup:
        subscriber.close()
        publisher.disconnect().join()
  }

  def "mqtt 5 client should be generate session with one pending QoS 2 packet"() {
    given:
        def publisher = buildExternalMqtt5Client()
        def subscriber = buildMqtt5MockClient()
        def subscriberId = generateClientId()
    when:
        publisher.connect().join()
        subscriber.connect()
        subscriber.send(new ConnectMqtt5OutMessage(subscriberId, keepAlive))
        def connectAck = subscriber.readNext() as ConnectAckMqttInMessage
    then:
        connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
    when:
        subscriber.send(new SubscribeMqtt5OutMessage(
            1,
            Array.of(Subscription.minimal(TopicFilter.valueOf("test/retry/$subscriberId"), QoS.EXACTLY_ONCE))))
        def subscribeAck = subscriber.readNext() as SubscribeAckMqttInMessage
    then:
        subscribeAck.reasonCodes.stream()
            .allMatch({ it == SubscribeAckReasonCode.GRANTED_QOS_2 })
    when:
        publisher.publishWith()
            .topic("test/retry/$subscriberId")
            .qos(MqttQos.AT_MOST_ONCE)
            .payload(publishPayload)
            .send()
            .join()

        def receivedPublish = subscriber.readNext() as PublishMqttInMessage
    then:
        receivedPublish.payload == publishPayload
    when:
        subscriber.disconnect()
        subscriber.connect()
        subscriber.send(new ConnectMqtt5OutMessage(subscriberId, keepAlive))
        connectAck = subscriber.readNext() as ConnectAckMqttInMessage
        def receivedDupPublish = subscriber.readNext() as PublishMqttInMessage
    then:
        connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
        receivedDupPublish.duplicate
        receivedDupPublish.messageId == receivedPublish.messageId
        receivedDupPublish.payload == publishPayload
    when:
        subscriber.disconnect()
        subscriber.connect()
        subscriber.send(new ConnectMqtt5OutMessage(subscriberId, keepAlive))
        connectAck = subscriber.readNext() as ConnectAckMqttInMessage
        receivedDupPublish = subscriber.readNext() as PublishMqttInMessage
        subscriber.send(new PublishReceivedMqtt5OutMessage(
            receivedDupPublish.messageId(),
            PublishReceivedReasonCode.SUCCESS
        ))

        def releaseAck = subscriber.readNext() as PublishReleaseMqttInMessage
        subscriber.send(new PublishCompleteMqtt5OutMessage(
            receivedDupPublish.messageId(),
            PublishCompletedReasonCode.SUCCESS
        ))
    then:
        connectAck.reasonCode == ConnectAckReasonCode.SUCCESS
        receivedDupPublish.duplicate
        receivedDupPublish.messageId == receivedPublish.messageId
        receivedDupPublish.payload == publishPayload
        releaseAck.messageId == receivedPublish.messageId
    cleanup:
        subscriber.close()
        publisher.disconnect().join()
  }
}
