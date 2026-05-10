package javasabr.mqtt.service.publish.processor

import java.time.Duration
import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttProtocolErrors
import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.DisconnectReasonCode
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode
import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode
import javasabr.mqtt.model.subscription.Subscription
import javasabr.mqtt.network.message.in.PublishReleaseMqttInMessage
import javasabr.mqtt.network.message.out.DisconnectMqtt5OutMessage
import javasabr.mqtt.network.message.out.MqttOutMessage
import javasabr.mqtt.network.message.out.PublishCompleteMqtt5OutMessage
import javasabr.mqtt.network.message.out.PublishMqtt5OutMessage
import javasabr.mqtt.network.message.out.PublishReceivedMqtt5OutMessage
import javasabr.mqtt.service.TestExternalNetworkMqttUser
import javasabr.mqtt.service.publish.exception.UnknownPublishStorageException
import javasabr.rlib.collections.array.Array

class Qos2IncomingPublishProcessorTest extends QosIncomingPublishProcessorTest {

  def "should provide feedback for accepted publish with subscribers"() {
    given:
        def processor = new Qos2IncomingPublishProcessor(
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
        def topicFilter = defaultTopicService.createTopicFilter(client1, "Qos2IncomingPublishProcessorTest/1")
        def expectedTopicName = defaultTopicService.createTopicName(client1, "Qos2IncomingPublishProcessorTest/1")
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
        def incomingPublish = preparePublish(
            expectedMessageId,
            QoS.EXACTLY_ONCE,
            expectedTopicName,
            testPayloadBytes)
    when:
        processor.process(client3, incomingPublish)
    then: 'sender should have feedback of first phase'
        with(client3.nextSentMessage(PublishReceivedMqtt5OutMessage)) {
          reasonCode() == PublishReceivedReasonCode.SUCCESS
          messageId() == expectedMessageId
          reason() == null
          userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        }
        with(inMessageTracker.stored(expectedMessageId)) {
          messageType() == MqttMessageType.PUBLISH
          reasonCode() == PublishReceivedReasonCode.SUCCESS
        }
    when:
        def publishRelease = PublishReleaseMqttInMessage
            .of(expectedMessageId, PublishReleaseReasonCode.SUCCESS)
        defaultPublishReleaseMqttInMessageHandler.processValidMessage(publisher, publishRelease)
    then: 'subscribers should receive the publish'
        with(client1.nextSentMessage(PublishMqtt5OutMessage)) {
          topicName() == expectedTopicName
        }
        with(client2.nextSentMessage(PublishMqtt5OutMessage)) {
          topicName() == expectedTopicName
        }
    then:
        with(client3.nextSentMessage(PublishCompleteMqtt5OutMessage)) {
          reasonCode() == PublishCompletedReasonCode.SUCCESS
          messageId() == expectedMessageId
          reason() == null
          userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        }
        inMessageTracker.stored(expectedMessageId) == null
  }
  
  //FIXME currently we can't resolve this status
  def "should provide feedback for accepted publish without any subscriber"() {
    given:
        def processor = new Qos2IncomingPublishProcessor(
            defaultSubscriptionService,
            defaultPublishDispatcher,
            defaultMessageOutFactoryService,
            defaultRetainMessageService,
            defaultIncomingPublishStorage)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = publisher.user() as TestExternalNetworkMqttUser
        def topicName = defaultTopicService.createTopicName(user, "Qos2IncomingPublishProcessorTest/2")
        def expectedMessageId = 35
        def session = user.session()
        def inMessageTracker = session.inMessageTracker()
        def incomingPublish = preparePublish(
            expectedMessageId,
            QoS.EXACTLY_ONCE,
            topicName,
            testPayloadBytes)
    when:
        processor.process(user, incomingPublish)
    then: 'sender should have feedback that no matched subscribers'
        with(user.nextSentMessage(PublishReceivedMqtt5OutMessage)) {
          reasonCode() == PublishReceivedReasonCode.SUCCESS
          messageId() == expectedMessageId
          reason() == null
          userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        }
        with(inMessageTracker.stored(expectedMessageId)) {
          messageType() == MqttMessageType.PUBLISH
          reasonCode() == PublishReceivedReasonCode.SUCCESS
        }
    when:
        def publishRelease = PublishReleaseMqttInMessage
            .of(expectedMessageId, PublishReleaseReasonCode.SUCCESS)
        defaultPublishReleaseMqttInMessageHandler.processValidMessage(publisher, publishRelease)
    then:
        with(user.nextSentMessage(PublishCompleteMqtt5OutMessage)) {
          reasonCode() == PublishCompletedReasonCode.SUCCESS
          messageId() == expectedMessageId
          reason() == null
          userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        }
        inMessageTracker.stored(expectedMessageId) == null
  }

  def "should disconnect by reason that message id is missed"() {
    given:
        def processor = new Qos2IncomingPublishProcessor(
            defaultSubscriptionService,
            defaultPublishDispatcher,
            defaultMessageOutFactoryService,
            defaultRetainMessageService,
            defaultIncomingPublishStorage)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = publisher.user() as TestExternalNetworkMqttUser
        def topicName = defaultTopicService.createTopicName(user, "Qos2IncomingPublishProcessorTest/3")
        def incomingPublish = preparePublish(
            MqttProperties.MESSAGE_ID_IS_NOT_SET,
            QoS.EXACTLY_ONCE,
            topicName,
            testPayloadBytes)
    when:
        processor.process(user, incomingPublish)
    then:
        with(user.nextSentMessage(DisconnectMqtt5OutMessage)) {
          reasonCode() == DisconnectReasonCode.PROTOCOL_ERROR
          reason() == MqttProtocolErrors.MISSED_REQUIRED_MESSAGE_ID
          userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        }
  }

  def "should provide feedback that message id is already used"() {
    given:
        def processor = new Qos2IncomingPublishProcessor(
            defaultSubscriptionService,
            defaultPublishDispatcher,
            defaultMessageOutFactoryService,
            defaultRetainMessageService,
            defaultIncomingPublishStorage)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = publisher.user() as TestExternalNetworkMqttUser
        def topicName = defaultTopicService.createTopicName(user, "Qos2IncomingPublishProcessorTest/4")
        def expectedMessageId = 35
        def session = user.session()
        def inMessageTracker = session.inMessageTracker()
        inMessageTracker.add(expectedMessageId, MqttMessageType.SUBSCRIBE)
        def incomingPublish = preparePublish(
            expectedMessageId,
            QoS.EXACTLY_ONCE,
            topicName,
            testPayloadBytes)
    when:
        processor.process(user, incomingPublish)
    then:
        with(user.nextSentMessage(PublishReceivedMqtt5OutMessage)) {
          reasonCode() == PublishReceivedReasonCode.PACKET_IDENTIFIER_IN_USE
          reason() == null
          userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        }
        with(inMessageTracker.stored(expectedMessageId)) {
          messageType() == MqttMessageType.SUBSCRIBE
        }
  }

  def "should provide feedback for duplicated publish as well"() {
    given:
        def processor = new Qos2IncomingPublishProcessor(
            defaultSubscriptionService,
            defaultPublishDispatcher,
            defaultMessageOutFactoryService,
            defaultRetainMessageService,
            defaultIncomingPublishStorage)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = publisher.user() as TestExternalNetworkMqttUser
        def topicName = defaultTopicService.createTopicName(user, "Qos2IncomingPublishProcessorTest/5")
        def expectedMessageId = 35
        def session = user.session()
        def inMessageTracker = session.inMessageTracker()
        inMessageTracker.add(expectedMessageId, MqttMessageType.PUBLISH, PublishReceivedReasonCode.NO_MATCHING_SUBSCRIBERS)
        def incomingPublish = preparePublish(
            expectedMessageId, 
            QoS.EXACTLY_ONCE,
            topicName,
            testPayloadBytes)
    when:
        processor.process(user, incomingPublish.withDuplicated())
    then:
        with(user.nextSentMessage(PublishReceivedMqtt5OutMessage)) {
          reasonCode() == PublishReceivedReasonCode.NO_MATCHING_SUBSCRIBERS
          reason() == null
          userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        }
        with(inMessageTracker.stored(expectedMessageId)) {
          messageType() == MqttMessageType.PUBLISH
        }
  }

  def "should not provide feedback for duplicated publish after accepting publish release"() {
    given:
        def processor = new Qos2IncomingPublishProcessor(
            defaultSubscriptionService,
            defaultPublishDispatcher,
            defaultMessageOutFactoryService,
            defaultRetainMessageService,
            defaultIncomingPublishStorage)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = publisher.user() as TestExternalNetworkMqttUser
        def topicName = defaultTopicService.createTopicName(user, "Qos2IncomingPublishProcessorTest/5")
        def expectedMessageId = 35
        def session = user.session()
        def inMessageTracker = session.inMessageTracker()
        def incomingPublish = preparePublish(
            expectedMessageId,
            QoS.EXACTLY_ONCE,
            topicName,
            testPayloadBytes)
        def duplicatedPublish = prepareDuplicatedPublish(
            expectedMessageId,
            QoS.EXACTLY_ONCE,
            topicName,
            testPayloadBytes)
    when: 'init floy by original publish'
        processor.process(user, incomingPublish)
    then:
        with(user.nextSentMessage(PublishReceivedMqtt5OutMessage)) {
          reasonCode() == PublishReceivedReasonCode.SUCCESS
          reason() == null
          userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        }
        with(inMessageTracker.stored(expectedMessageId)) {
          messageType() == MqttMessageType.PUBLISH
          reasonCode() == PublishReceivedReasonCode.SUCCESS
        }
    when: 'send duplicated before publish release'
        processor.process(user, duplicatedPublish.withDuplicated())
    then: 'server should return the same feedback for duplicated as for original'
        with(user.nextSentMessage(PublishReceivedMqtt5OutMessage)) {
          reasonCode() == PublishReceivedReasonCode.SUCCESS
          reason() == null
          userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        }
        with(inMessageTracker.stored(expectedMessageId)) {
          messageType() == MqttMessageType.PUBLISH
          reasonCode() == PublishReceivedReasonCode.SUCCESS
        }
    when: 'send publish release to change flow stage'
        def publishRelease = PublishReleaseMqttInMessage
            .of(expectedMessageId, PublishReleaseReasonCode.SUCCESS)
        user.returnCompletedFeatures(false)
        defaultPublishReleaseMqttInMessageHandler.processValidMessage(publisher, publishRelease)
    then:
        with(user.nextSentMessage(PublishCompleteMqtt5OutMessage)) {
          reasonCode() == PublishCompletedReasonCode.SUCCESS
          messageId() == expectedMessageId
          reason() == null
          userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        }
        with(inMessageTracker.stored(expectedMessageId)) {
          messageType() == MqttMessageType.PUBLISH_COMPLETE
        }
    when: 'send duplicated after publish release'
        processor.process(user, incomingPublish.withDuplicated())
    then: 'server should return that this message id is already used because publish complete is in progress of sending'
        with(user.nextSentMessage(PublishReceivedMqtt5OutMessage)) {
          reasonCode() == PublishReceivedReasonCode.PACKET_IDENTIFIER_IN_USE
          reason() == null
          userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        }
  }

  def "should provide packet identifier not found for too late publish release"() {
    given:
        def processor = new Qos2IncomingPublishProcessor(
            defaultSubscriptionService,
            defaultPublishDispatcher,
            defaultMessageOutFactoryService,
            defaultRetainMessageService,
            defaultIncomingPublishStorage)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = publisher.user() as TestExternalNetworkMqttUser
        def topicName = defaultTopicService.createTopicName(user, "Qos2IncomingPublishProcessorTest/6")
        def expectedMessageId = 35
        def session = user.session()
        def inMessageTracker = session.inMessageTracker()
        def incomingPublish = preparePublish(
            expectedMessageId,
            QoS.EXACTLY_ONCE,
            topicName,
            testPayloadBytes)
    when:
        processor.process(user, incomingPublish)
    then:
        with(user.nextSentMessage(PublishReceivedMqtt5OutMessage)) {
          reasonCode() == PublishReceivedReasonCode.SUCCESS
          messageId() == expectedMessageId
        }
        with(inMessageTracker.stored(expectedMessageId)) {
          messageType() == MqttMessageType.PUBLISH
          reasonCode() == PublishReceivedReasonCode.SUCCESS
        }
    when: 'simulate timeout cleanup before PUBREL arrives'
        defaultIncomingPublishStorage.cancelScheduledRemoval(incomingPublish)
        def publishRelease = PublishReleaseMqttInMessage
            .of(expectedMessageId, PublishReleaseReasonCode.SUCCESS)
        defaultPublishReleaseMqttInMessageHandler.processValidMessage(publisher, publishRelease)
    then:
        with(user.nextSentMessage(PublishCompleteMqtt5OutMessage)) {
          reasonCode() == PublishCompletedReasonCode.PACKET_IDENTIFIER_NOT_FOUND
          messageId() == expectedMessageId
          reason() == null
          userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        }
        inMessageTracker.stored(expectedMessageId) == null
        session.incomingProcessingPublishes().size() == 0
  }

  def "should drop publish without feedback when publish release arrives without tracked message meta"() {
    given:
        def processor = new Qos2IncomingPublishProcessor(
            defaultSubscriptionService,
            defaultPublishDispatcher,
            defaultMessageOutFactoryService,
            defaultRetainMessageService,
            defaultIncomingPublishStorage)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = publisher.user() as TestExternalNetworkMqttUser
        def topicName = defaultTopicService.createTopicName(user, "Qos2IncomingPublishProcessorTest/7")
        def expectedMessageId = 35
        def session = user.session()
        def inMessageTracker = session.inMessageTracker()
        def incomingPublish = preparePublish(
            expectedMessageId,
            QoS.EXACTLY_ONCE,
            topicName,
            testPayloadBytes)
    when:
        processor.process(user, incomingPublish)
    then:
        with(user.nextSentMessage(PublishReceivedMqtt5OutMessage)) {
          reasonCode() == PublishReceivedReasonCode.SUCCESS
          messageId() == expectedMessageId
        }
        with(inMessageTracker.stored(expectedMessageId)) {
          messageType() == MqttMessageType.PUBLISH
          reasonCode() == PublishReceivedReasonCode.SUCCESS
        }
    when: 'tracked message meta is missing but callback is still registered'
        inMessageTracker.remove(expectedMessageId)
        def publishRelease = PublishReleaseMqttInMessage
            .of(expectedMessageId, PublishReleaseReasonCode.SUCCESS)
        defaultPublishReleaseMqttInMessageHandler.processValidMessage(publisher, publishRelease)
    then:
        user.isEmpty()
        inMessageTracker.stored(expectedMessageId) == null
        session.incomingProcessingPublishes().size() == 0
    when:
        defaultIncomingPublishStorage.scheduleRemoval(incomingPublish, Duration.ofSeconds(1))
    then:
        def exception = thrown(UnknownPublishStorageException)
        exception.id() == incomingPublish.id()
  }
}
