package javasabr.mqtt.service.publish.processor

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttProtocolErrors
import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.DisconnectReasonCode
import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.mqtt.model.subscription.Subscription
import javasabr.mqtt.network.message.out.DisconnectMqtt5OutMessage
import javasabr.mqtt.network.message.out.MqttOutMessage
import javasabr.mqtt.network.message.out.PublishAckMqtt5OutMessage
import javasabr.mqtt.network.message.out.PublishMqtt5OutMessage
import javasabr.mqtt.service.TestExternalNetworkMqttUser
import javasabr.rlib.collections.array.Array

import static javasabr.mqtt.model.subscription.TestPublishFactory.incomingPublish

class Qos1IncomingPublishProcessorTest extends QosIncomingPublishProcessorTest {

  def "should provide feedback for accepted publish with subscribers"() {
    given:
        def processor = new Qos1IncomingPublishProcessor(
            defaultSubscriptionService,
            defaultPublishDispatcher,
            defaultMessageOutFactoryService,
            defaultRetainMessageService,
            defaultIncomingPublishStorage)
        def subscriber1 = mockedExternalConnection(MqttVersion.MQTT_5)
        def subscriber2 = mockedExternalConnection(MqttVersion.MQTT_5)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def client1 = subscriber1.user() as TestExternalNetworkMqttUser
        def client2 = subscriber2.user() as TestExternalNetworkMqttUser
        def client3 = publisher.user() as TestExternalNetworkMqttUser
        def topicFilter = defaultTopicService.createTopicFilter(client1, "Qos1IncomingPublishProcessorTest/1")
        def expectedTopicName = defaultTopicService.createTopicName(client1, "Qos1IncomingPublishProcessorTest/1")
        def expectedMessageId = 35
        defaultSubscriptionService.subscribe(
            client1,
            client1.session(),
            Array.of(Subscription.minimal(topicFilter, QoS.AT_MOST_ONCE)))
        defaultSubscriptionService.subscribe(
            client2,
            client2.session(),
            Array.of(Subscription.minimal(topicFilter, QoS.AT_MOST_ONCE)))
        def inMessageTracker = client3
            .session()
            .inMessageTracker()
    when:
        processor.process(client3, incomingPublish(expectedMessageId, QoS.AT_MOST_ONCE, expectedTopicName, testPayload))
    then: 'sender should have feedback'
        with(client3.nextSentMessage(PublishAckMqtt5OutMessage)) {
          reasonCode() == PublishAckReasonCode.SUCCESS
          messageId() == expectedMessageId
          reason() == null
          userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        }
        inMessageTracker.stored(expectedMessageId) == null
    then: 'subscribers should receive the publish'
        with(client1.nextSentMessage(PublishMqtt5OutMessage)) {
          topicName() == expectedTopicName
        }
        with(client2.nextSentMessage(PublishMqtt5OutMessage)) {
          topicName() == expectedTopicName
        }
  }

  def "should provide feedback for accepted publish without any subscriber"() {
    given:
        def processor = new Qos1IncomingPublishProcessor(
            defaultSubscriptionService,
            defaultPublishDispatcher,
            defaultMessageOutFactoryService,
            defaultRetainMessageService,
            defaultIncomingPublishStorage)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = publisher.user() as TestExternalNetworkMqttUser
        def topicName = defaultTopicService.createTopicName(user, "Qos1IncomingPublishProcessorTest/2")
        def expectedMessageId = 35
        def session = user.session()
        def inMessageTracker = session.inMessageTracker()
    when:
        processor.process(user, incomingPublish(expectedMessageId, QoS.AT_MOST_ONCE, topicName, testPayload))
    then: 'sender should have feedback that no matched subscribers'
        with(user.nextSentMessage(PublishAckMqtt5OutMessage)) {
          reasonCode() == PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS
          messageId() == expectedMessageId
          reason() == null
          userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        }
        inMessageTracker.stored(expectedMessageId) == null
  }

  def "should disconnect by reason that message id is missed"() {
    given:
        def processor = new Qos1IncomingPublishProcessor(
            defaultSubscriptionService,
            defaultPublishDispatcher,
            defaultMessageOutFactoryService,
            defaultRetainMessageService,
            defaultIncomingPublishStorage)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = publisher.user() as TestExternalNetworkMqttUser
        def topicName = defaultTopicService.createTopicName(user, "Qos1IncomingPublishProcessorTest/3")
    when:
        processor.process(user, incomingPublish(
            MqttProperties.MESSAGE_ID_IS_NOT_SET,
            QoS.AT_MOST_ONCE,
            topicName,
            testPayload))
    then:
        with(user.nextSentMessage(DisconnectMqtt5OutMessage)) {
          reasonCode() == DisconnectReasonCode.PROTOCOL_ERROR
          reason() == MqttProtocolErrors.MISSED_REQUIRED_MESSAGE_ID
          userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        }
  }

  def "should provide feedback that message id is already used"() {
    given:
        def processor = new Qos1IncomingPublishProcessor(
            defaultSubscriptionService,
            defaultPublishDispatcher,
            defaultMessageOutFactoryService,
            defaultRetainMessageService,
            defaultIncomingPublishStorage)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = publisher.user() as TestExternalNetworkMqttUser
        def topicName = defaultTopicService.createTopicName(user, "Qos1IncomingPublishProcessorTest/4")
        def expectedMessageId = 35
        def session = user.session()
        def inMessageTracker = session.inMessageTracker()
        inMessageTracker.add(expectedMessageId, MqttMessageType.SUBSCRIBE)
    when:
        processor.process(user, incomingPublish(expectedMessageId, QoS.AT_MOST_ONCE, topicName, testPayload))
    then:
        with(user.nextSentMessage(PublishAckMqtt5OutMessage)) {
          reasonCode() == PublishAckReasonCode.PACKET_IDENTIFIER_IN_USE
          reason() == null
          userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        }
        with(inMessageTracker.stored(expectedMessageId)) {
          messageType() == MqttMessageType.SUBSCRIBE
        }
  }

  def "should skip handling duplicated publish"() {
    given:
        def processor = new Qos1IncomingPublishProcessor(
            defaultSubscriptionService,
            defaultPublishDispatcher,
            defaultMessageOutFactoryService,
            defaultRetainMessageService,
            defaultIncomingPublishStorage)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = publisher.user() as TestExternalNetworkMqttUser
        def topicName = defaultTopicService.createTopicName(user, "Qos1IncomingPublishProcessorTest/5")
        def expectedMessageId = 35
        def session = user.session()
        def inMessageTracker = session.inMessageTracker()
        inMessageTracker.add(expectedMessageId, MqttMessageType.PUBLISH)
    when:
        processor.process(
            user, 
            incomingPublish(expectedMessageId, QoS.AT_MOST_ONCE, topicName, testPayload)
                .withDuplicated())
    then:
        user.isEmpty()
  }
}
