package javasabr.mqtt.service.publish.handler.impl

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttProtocolErrors
import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.publishing.Publish
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
import javasabr.rlib.collections.array.Array

class Qos2MqttPublishInMessageHandlerTest extends QosMqttPublishInMessageHandlerTest {

  def "should provide feedback for accepted publish with subscribers"() {
    given:
        def publishInHandler = new Qos2MqttPublishInMessageHandler(
            defaultSubscriptionService,
            defaultPublishDeliveringService,
            defaultMessageOutFactoryService)
        def subscriber1 = mockedExternalConnection(MqttVersion.MQTT_5)
        def subscriber2 = mockedExternalConnection(MqttVersion.MQTT_5)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def client1 = subscriber1.user() as TestExternalNetworkMqttUser
        def client2 = subscriber2.user() as TestExternalNetworkMqttUser
        def client3 = publisher.user() as TestExternalNetworkMqttUser
        def topicFilter = defaultTopicService.createTopicFilter(client1, "Qos2MqttPublishInMessageHandlerTest/1")
        def topicName = defaultTopicService.createTopicName(client1, "Qos2MqttPublishInMessageHandlerTest/1")
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
        publishInHandler.handle(client3, Publish.minimal(expectedMessageId, QoS.EXACTLY_ONCE, topicName, testPayload))
    then: 'sender should have feedback of first phase'
        def publishReceive = client3.nextSentMessage(PublishReceivedMqtt5OutMessage)
        publishReceive.reasonCode() == PublishReceivedReasonCode.SUCCESS
        publishReceive.messageId() == expectedMessageId
        publishReceive.reason() == null
        publishReceive.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        def trackedMessageMeta = inMessageTracker.stored(expectedMessageId)
        trackedMessageMeta != null
        trackedMessageMeta.messageType() == MqttMessageType.PUBLISH
        trackedMessageMeta.reasonCode() == PublishReceivedReasonCode.SUCCESS
    then: 'subscribers should receive the publish'
        def message1 = client1.nextSentMessage(PublishMqtt5OutMessage)
        message1.topicName() == topicName
        def message2 = client2.nextSentMessage(PublishMqtt5OutMessage)
        message2.topicName() == topicName
    when:
        def publishRelease = new PublishReleaseMqttInMessage(0b0000_0010 as byte) {{
          messageId = expectedMessageId
          reasonCode = PublishReleaseReasonCode.SUCCESS
        }}
        defaultPublishReleaseMqttInMessageHandler.processValidMessage(publisher, publishRelease)
    then:
        def publishComplete = client3.nextSentMessage(PublishCompleteMqtt5OutMessage)
        publishComplete.reasonCode() == PublishCompletedReasonCode.SUCCESS
        publishComplete.messageId() == expectedMessageId
        publishComplete.reason() == null
        publishComplete.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        inMessageTracker.stored(expectedMessageId) == null
  }

  def "should provide feedback for accepted publish without any subscriber"() {
    given:
        def publishInHandler = new Qos2MqttPublishInMessageHandler(
            defaultSubscriptionService,
            defaultPublishDeliveringService,
            defaultMessageOutFactoryService)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = publisher.user() as TestExternalNetworkMqttUser
        def topicName = defaultTopicService.createTopicName(user, "Qos2MqttPublishInMessageHandlerTest/2")
        def expectedMessageId = 35
        def session = user.session()
        def inMessageTracker = session.inMessageTracker()
    when:
        publishInHandler.handle(user, Publish.minimal(expectedMessageId, QoS.EXACTLY_ONCE, topicName, testPayload))
    then: 'sender should have feedback that no matched subscribers'
        def publishReceive = user.nextSentMessage(PublishReceivedMqtt5OutMessage)
        publishReceive.reasonCode() == PublishReceivedReasonCode.NO_MATCHING_SUBSCRIBERS
        publishReceive.messageId() == expectedMessageId
        publishReceive.reason() == null
        publishReceive.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        def trackedMessageMeta = inMessageTracker.stored(expectedMessageId)
        trackedMessageMeta != null
        trackedMessageMeta.messageType() == MqttMessageType.PUBLISH
        trackedMessageMeta.reasonCode() == PublishReceivedReasonCode.NO_MATCHING_SUBSCRIBERS
    when:
        def publishRelease = new PublishReleaseMqttInMessage(0b0000_0010 as byte) {{
          messageId = expectedMessageId
          reasonCode = PublishReleaseReasonCode.SUCCESS
        }}
        defaultPublishReleaseMqttInMessageHandler.processValidMessage(publisher, publishRelease)
    then:
        def publishComplete = user.nextSentMessage(PublishCompleteMqtt5OutMessage)
        publishComplete.reasonCode() == PublishCompletedReasonCode.SUCCESS
        publishComplete.messageId() == expectedMessageId
        publishComplete.reason() == null
        publishComplete.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        inMessageTracker.stored(expectedMessageId) == null
  }

  def "should disconnect by reason that message id is missed"() {
    given:
        def publishInHandler = new Qos2MqttPublishInMessageHandler(
            defaultSubscriptionService,
            defaultPublishDeliveringService,
            defaultMessageOutFactoryService)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = publisher.user() as TestExternalNetworkMqttUser
        def topicName = defaultTopicService.createTopicName(user, "Qos2MqttPublishInMessageHandlerTest/3")
    when:
        publishInHandler.handle(user, Publish.minimal(
            MqttProperties.MESSAGE_ID_IS_NOT_SET,
            QoS.EXACTLY_ONCE,
            topicName,
            testPayload))
    then:
        def disconnect = user.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnect.reasonCode() == DisconnectReasonCode.PROTOCOL_ERROR
        disconnect.reason() == MqttProtocolErrors.MISSED_REQUIRED_MESSAGE_ID
        disconnect.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
  }

  def "should provide feedback that message id is already used"() {
    given:
        def publishInHandler = new Qos2MqttPublishInMessageHandler(
            defaultSubscriptionService,
            defaultPublishDeliveringService,
            defaultMessageOutFactoryService)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = publisher.user() as TestExternalNetworkMqttUser
        def topicName = defaultTopicService.createTopicName(user, "Qos2MqttPublishInMessageHandlerTest/4")
        def expectedMessageId = 35
        def session = user.session()
        def inMessageTracker = session.inMessageTracker()
        inMessageTracker.add(expectedMessageId, MqttMessageType.SUBSCRIBE)
    when:
        publishInHandler.handle(user, Publish.minimal(expectedMessageId, QoS.EXACTLY_ONCE, topicName, testPayload))
    then:
        def publishReceive = user.nextSentMessage(PublishReceivedMqtt5OutMessage)
        publishReceive.reasonCode() == PublishReceivedReasonCode.PACKET_IDENTIFIER_IN_USE
        publishReceive.reason() == null
        publishReceive.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        def trackedMessageMeta = inMessageTracker.stored(expectedMessageId)
        trackedMessageMeta != null
        trackedMessageMeta.messageType() == MqttMessageType.SUBSCRIBE
  }

  def "should provide feedback for duplicated publish as well"() {
    given:
        def publishInHandler = new Qos2MqttPublishInMessageHandler(
            defaultSubscriptionService,
            defaultPublishDeliveringService,
            defaultMessageOutFactoryService)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = publisher.user() as TestExternalNetworkMqttUser
        def topicName = defaultTopicService.createTopicName(user, "Qos2MqttPublishInMessageHandlerTest/5")
        def expectedMessageId = 35
        def session = user.session()
        def inMessageTracker = session.inMessageTracker()
        inMessageTracker.add(expectedMessageId, MqttMessageType.PUBLISH, PublishReceivedReasonCode.NO_MATCHING_SUBSCRIBERS)
    when:
        publishInHandler.handle(user, Publish
            .minimal(expectedMessageId, QoS.EXACTLY_ONCE, topicName, testPayload)
            .withDuplicated())
    then:
        def publishReceive = user.nextSentMessage(PublishReceivedMqtt5OutMessage)
        publishReceive.reasonCode() == PublishReceivedReasonCode.NO_MATCHING_SUBSCRIBERS
        publishReceive.reason() == null
        publishReceive.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        def trackedMessageMeta = inMessageTracker.stored(expectedMessageId)
        trackedMessageMeta != null
        trackedMessageMeta.messageType() == MqttMessageType.PUBLISH
  }

  def "should not provide feedback for duplicated publish after accepting publish release"() {
    given:
        def publishInHandler = new Qos2MqttPublishInMessageHandler(
            defaultSubscriptionService,
            defaultPublishDeliveringService,
            defaultMessageOutFactoryService)
        def publisher = mockedExternalConnection(MqttVersion.MQTT_5)
        def user = publisher.user() as TestExternalNetworkMqttUser
        def topicName = defaultTopicService.createTopicName(user, "Qos2MqttPublishInMessageHandlerTest/5")
        def expectedMessageId = 35
        def session = user.session()
        def inMessageTracker = session.inMessageTracker()
    when: 'init floy by original publish'
        publishInHandler.handle(user, Publish.minimal(expectedMessageId, QoS.EXACTLY_ONCE, topicName, testPayload))
    then:
        def publishReceive = user.nextSentMessage(PublishReceivedMqtt5OutMessage)
        publishReceive.reasonCode() == PublishReceivedReasonCode.NO_MATCHING_SUBSCRIBERS
        publishReceive.reason() == null
        publishReceive.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        def trackedMessageMeta = inMessageTracker.stored(expectedMessageId)
        trackedMessageMeta != null
        trackedMessageMeta.messageType() == MqttMessageType.PUBLISH
        trackedMessageMeta.reasonCode() == PublishReceivedReasonCode.NO_MATCHING_SUBSCRIBERS
    when: 'send duplicated before publish release'
        publishInHandler.handle(user, Publish
            .minimal(expectedMessageId, QoS.EXACTLY_ONCE, topicName, testPayload)
            .withDuplicated())
    then: 'server should return the same feedback for duplicated as for original'
        def publishReceive2 = user.nextSentMessage(PublishReceivedMqtt5OutMessage)
        publishReceive2.reasonCode() == PublishReceivedReasonCode.NO_MATCHING_SUBSCRIBERS
        publishReceive2.reason() == null
        publishReceive2.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        def trackedMessageMeta2 = inMessageTracker.stored(expectedMessageId)
        trackedMessageMeta2 != null
        trackedMessageMeta2.messageType() == MqttMessageType.PUBLISH
        trackedMessageMeta2.reasonCode() == PublishReceivedReasonCode.NO_MATCHING_SUBSCRIBERS
    when: 'send publish release to change flow stage'
        def publishRelease = new PublishReleaseMqttInMessage(0b0000_0010 as byte) {{
          messageId = expectedMessageId
          reasonCode = PublishReleaseReasonCode.SUCCESS
        }}
        user.returnCompletedFeatures(false)
        defaultPublishReleaseMqttInMessageHandler.processValidMessage(publisher, publishRelease)
    then:
        def publishComplete = user.nextSentMessage(PublishCompleteMqtt5OutMessage)
        publishComplete.reasonCode() == PublishCompletedReasonCode.SUCCESS
        publishComplete.messageId() == expectedMessageId
        publishComplete.reason() == null
        publishComplete.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
        def trackedMessageMeta3 = inMessageTracker.stored(expectedMessageId)
        trackedMessageMeta3 != null
        trackedMessageMeta3.messageType() == MqttMessageType.PUBLISH_COMPLETE
    when: 'send duplicated after publish release'
        publishInHandler.handle(user, Publish
            .minimal(expectedMessageId, QoS.EXACTLY_ONCE, topicName, testPayload)
            .withDuplicated())
    then: 'server should return that this message id is already used because publish complete is in progress of sending'
        def publishReceive3 = user.nextSentMessage(PublishReceivedMqtt5OutMessage)
        publishReceive3.reasonCode() == PublishReceivedReasonCode.PACKET_IDENTIFIER_IN_USE
        publishReceive3.reason() == null
        publishReceive3.userProperties() == MqttOutMessage.EMPTY_USER_PROPERTIES
  }
}
