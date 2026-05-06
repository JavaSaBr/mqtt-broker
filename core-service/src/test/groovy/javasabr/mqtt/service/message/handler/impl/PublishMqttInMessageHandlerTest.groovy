package javasabr.mqtt.service.message.handler.impl

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.MqttProtocolErrors
import javasabr.mqtt.model.MqttVersion
import javasabr.mqtt.model.PayloadFormat
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.reason.code.DisconnectReasonCode
import javasabr.mqtt.model.reason.code.PublishAckReasonCode
import javasabr.mqtt.network.message.in.PublishMqttInMessage
import javasabr.mqtt.network.message.out.DisconnectMqtt5OutMessage
import javasabr.mqtt.network.message.out.PublishAckMqtt5OutMessage
import javasabr.mqtt.network.util.ExtraErrorReasons
import javasabr.mqtt.service.IntegrationServiceSpecification
import javasabr.mqtt.service.TestExternalNetworkMqttUser

class PublishMqttInMessageHandlerTest extends IntegrationServiceSpecification {

  def "should update topic name alias mapping in session in MQTT 5"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new PublishMqttInMessageHandler(
            publishReceivingService,
            defaultMessageOutFactoryService,
            defaultTopicService,
            disabledAclService,
            defaultPublishDataStorage)
        def expectedMessageId = 15
        def expectedTopicAlias = 5
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
        def expectedTopicName = defaultTopicService.createTopicName(mqttUser, "topic/name1")
    when:
        def publishMessage = new PublishMqttInMessage(0b0110_0011 as byte) {{
          messageId = expectedMessageId
          payload = [1, 2, 3, 4, 5]
          rawTopicName = expectedTopicName.rawTopic()
          topicAlias = expectedTopicAlias
        }}
        messageHandler.processValidMessage(mqttConnection, publishMessage)
    then:
        def publishAck = mqttUser.nextSentMessage(PublishAckMqtt5OutMessage)
        publishAck.reasonCode() == PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS
        publishAck.reason() == null
        mqttUser.session()
            .topicNameMapping()
            .resolve(expectedTopicAlias) == expectedTopicName
  }

  def "should track messageId for QoS 1"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new PublishMqttInMessageHandler(
            publishReceivingService,
            defaultMessageOutFactoryService,
            defaultTopicService,
            disabledAclService,
            defaultPublishDataStorage)
        def expectedMessageId = 15
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
        mqttUser.returnCompletedFeatures(false)
        def session = mqttUser.session()
        def inMessageTracker = session.inMessageTracker()
    when:
        def publishMessage = new PublishMqttInMessage(0b0110_0011 as byte) {{
          messageId = expectedMessageId
          payload = [1, 2, 3, 4, 5]
          rawTopicName = "topic/name1"
        }}
        messageHandler.processValidMessage(mqttConnection, publishMessage)
    then:
        def publishAck = mqttUser.nextSentMessage(PublishAckMqtt5OutMessage)
        publishAck.reasonCode() == PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS
        publishAck.reason() == null
        inMessageTracker.stored(expectedMessageId) != null
  }

  def "should close connection by reason that session is already closed"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new PublishMqttInMessageHandler(
            publishReceivingService,
            defaultMessageOutFactoryService,
            defaultTopicService,
            disabledAclService,
            defaultPublishDataStorage)
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
        mqttUser.session(null)
    when:
        def publishMessage = new PublishMqttInMessage(0b0110_0010 as byte)
        messageHandler.processValidMessage(mqttConnection, publishMessage)
    then:
        def disconnectReason = mqttUser.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnectReason.reasonCode() == DisconnectReasonCode.UNSPECIFIED_ERROR
        disconnectReason.reason() == ExtraErrorReasons.SESSION_IS_ALREADY_CLOSED
        disconnectReason.serverReference() == null
  }

  def "should response that message id is missed for QoS 1"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new PublishMqttInMessageHandler(
            publishReceivingService,
            defaultMessageOutFactoryService,
            defaultTopicService,
            disabledAclService,
            defaultPublishDataStorage)
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
    when:
        def publishMessage = new PublishMqttInMessage(0b0110_0010 as byte) {{
          payload = testPayloadBytes
          rawTopicName = "/topic/1"
        }}
        messageHandler.processValidMessage(mqttConnection, publishMessage)
    then:
        def disconnectReason = mqttUser.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnectReason.reasonCode() == DisconnectReasonCode.PROTOCOL_ERROR
        disconnectReason.reason() == MqttProtocolErrors.MISSED_REQUIRED_MESSAGE_ID
        disconnectReason.serverReference() == null
  }

  def "should response that message id is already in use for QoS 1"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new PublishMqttInMessageHandler(
            publishReceivingService,
            defaultMessageOutFactoryService,
            defaultTopicService,
            disabledAclService,
            defaultPublishDataStorage)
        def expectedMessageId = 15
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
        mqttUser
            .session()
            .inMessageTracker()
            .add(expectedMessageId, MqttMessageType.PUBLISH)
    when:
        def publishMessage = new PublishMqttInMessage(0b0110_0010 as byte) {{
            messageId = expectedMessageId
            payload = testPayloadBytes
            rawTopicName = "/topic/1"
        }}
        messageHandler.processValidMessage(mqttConnection, publishMessage)
    then:
        def publishAck = mqttUser.nextSentMessage(PublishAckMqtt5OutMessage)
        publishAck.reasonCode() == PublishAckReasonCode.PACKET_IDENTIFIER_IN_USE
        publishAck.reason() == null
  }

  def "should skip publish without any payload"() {
    given:
        def connectionConfig = defaultExternalServerConnectionConfig
            .withMaxQos(QoS.AT_MOST_ONCE)
        def mqttConnection = mockedExternalConnection(connectionConfig, MqttVersion.MQTT_5)
        def messageHandler = new PublishMqttInMessageHandler(
            publishReceivingService,
            defaultMessageOutFactoryService,
            defaultTopicService,
            disabledAclService,
            defaultPublishDataStorage)
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
    when:
        def publishMessage = new PublishMqttInMessage(0 as byte)
        messageHandler.processValidMessage(mqttConnection, publishMessage)
    then:
        mqttUser.isEmpty()
  }

  def "should response that requested QoS 1 is not supported"() {
    given:
        def connectionConfig = defaultExternalServerConnectionConfig
            .withMaxQos(QoS.AT_MOST_ONCE)
        def mqttConnection = mockedExternalConnection(connectionConfig, MqttVersion.MQTT_5)
        def messageHandler = new PublishMqttInMessageHandler(
            publishReceivingService,
            defaultMessageOutFactoryService,
            defaultTopicService,
            disabledAclService,
            defaultPublishDataStorage)
        def expectedMessageId = 15
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
    when:
        def publishMessage = new PublishMqttInMessage(0b0110_0011 as byte) {{
          messageId = expectedMessageId
          payload = [1, 2, 3, 4, 5]
        }}
        messageHandler.processValidMessage(mqttConnection, publishMessage)
    then:
        def disconnectReason = mqttUser.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnectReason.reasonCode() == DisconnectReasonCode.QOS_NOT_SUPPORTED
        disconnectReason.reason() == null
        disconnectReason.serverReference() == null
  }

  def "should response that 'RETAIN' is not supported"() {
    given:
        def connectionConfig = defaultExternalServerConnectionConfig
            .withRetainAvailable(false)
        def mqttConnection = mockedExternalConnection(connectionConfig, MqttVersion.MQTT_5)
        def messageHandler = new PublishMqttInMessageHandler(
            publishReceivingService,
            defaultMessageOutFactoryService,
            defaultTopicService,
            disabledAclService,
            defaultPublishDataStorage)
        def expectedMessageId = 15
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
    when:
        def publishMessage = new PublishMqttInMessage(0b0110_0011 as byte) {{
          messageId = expectedMessageId
          payload = [1, 2, 3, 4, 5]
        }}
        messageHandler.processValidMessage(mqttConnection, publishMessage)
    then:
        def disconnectReason = mqttUser.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnectReason.reasonCode() == DisconnectReasonCode.RETAIN_NOT_SUPPORTED
        disconnectReason.reason() == null
        disconnectReason.serverReference() == null
  }

  def "should response that payload format is invalid"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new PublishMqttInMessageHandler(
            publishReceivingService,
            defaultMessageOutFactoryService,
            defaultTopicService,
            disabledAclService,
            defaultPublishDataStorage)
        def expectedMessageId = 15
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
    when:
        def publishMessage = new PublishMqttInMessage(0b0110_0011 as byte) {{
          messageId = expectedMessageId
          payload = [1, 2, 3, 4, 5]
          payloadFormat = PayloadFormat.INVALID
        }}
        messageHandler.processValidMessage(mqttConnection, publishMessage)
    then:
        def disconnectReason = mqttUser.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnectReason.reasonCode() == DisconnectReasonCode.PROTOCOL_ERROR
        disconnectReason.reason() == MqttProtocolErrors.PROVIDED_INVALID_PAYLOAD_FORMAT
        disconnectReason.serverReference() == null
  }

  def "should response that message expiry interval is invalid"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new PublishMqttInMessageHandler(
            publishReceivingService,
            defaultMessageOutFactoryService,
            defaultTopicService,
            disabledAclService,
            defaultPublishDataStorage)
        def expectedMessageId = 15
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
    when:
        def publishMessage = new PublishMqttInMessage(0b0110_0011 as byte) {{
          messageId = expectedMessageId
          payload = [1, 2, 3, 4, 5]
          messageExpiryInterval = -100
        }}
        messageHandler.processValidMessage(mqttConnection, publishMessage)
    then:
        def disconnectReason = mqttUser.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnectReason.reasonCode() == DisconnectReasonCode.PROTOCOL_ERROR
        disconnectReason.reason() == MqttProtocolErrors.PROVIDED_INVALID_MESSAGE_EXPIRY_INTERVAL
        disconnectReason.serverReference() == null
  }

  def "should response that response topic name is invalid"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new PublishMqttInMessageHandler(
            publishReceivingService,
            defaultMessageOutFactoryService,
            defaultTopicService,
            disabledAclService,
            defaultPublishDataStorage)
        def expectedMessageId = 15
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
    when:
        def publishMessage = new PublishMqttInMessage(0b0110_0011 as byte) {{
          messageId = expectedMessageId
          payload = [1, 2, 3, 4, 5]
          rawResponseTopicName = "topic###"
        }}
        messageHandler.processValidMessage(mqttConnection, publishMessage)
    then:
        def disconnectReason = mqttUser.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnectReason.reasonCode() == DisconnectReasonCode.PROTOCOL_ERROR
        disconnectReason.reason() == MqttProtocolErrors.PROVIDED_INVALID_RESPONSE_TOPIC
        disconnectReason.serverReference() == null
  }

  def "should response that no any information about topic name"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new PublishMqttInMessageHandler(
            publishReceivingService,
            defaultMessageOutFactoryService,
            defaultTopicService,
            disabledAclService,
            defaultPublishDataStorage)
        def expectedMessageId = 15
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
    when:
        def publishMessage = new PublishMqttInMessage(0b0110_0011 as byte) {{
          messageId = expectedMessageId
          payload = [1, 2, 3, 4, 5]
        }}
        messageHandler.processValidMessage(mqttConnection, publishMessage)
    then:
        def disconnectReason = mqttUser.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnectReason.reasonCode() == DisconnectReasonCode.PROTOCOL_ERROR
        disconnectReason.reason() == MqttProtocolErrors.NO_ANY_TOPIC_NANE
        disconnectReason.serverReference() == null
  }

  def "should response that topic alias is invalid"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new PublishMqttInMessageHandler(
            publishReceivingService,
            defaultMessageOutFactoryService,
            defaultTopicService,
            disabledAclService,
            defaultPublishDataStorage)
        def expectedMessageId = 15
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
    when: 'topic alias is too high'
        def publishMessage = new PublishMqttInMessage(0b0110_0011 as byte) {{
          messageId = expectedMessageId
          payload = [1, 2, 3, 4, 5]
          topicAlias = MqttProperties.TOPIC_ALIAS_MAX_DEFAULT + 10
        }}
        messageHandler.processValidMessage(mqttConnection, publishMessage)
    then:
        def disconnectReason = mqttUser.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnectReason.reasonCode() == DisconnectReasonCode.TOPIC_ALIAS_INVALID
        disconnectReason.reason() == null
        disconnectReason.serverReference() == null
    when: 'topic alias is too low'
        def publishMessage2 = new PublishMqttInMessage(0b0110_0011 as byte) {{
          messageId = expectedMessageId
          payload = [1, 2, 3, 4, 5]
          topicAlias = MqttProperties.TOPIC_ALIAS_MIN - 10
        }}
        messageHandler.processValidMessage(mqttConnection, publishMessage2)
    then:
        def disconnectReason2 = mqttUser.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnectReason2.reasonCode() == DisconnectReasonCode.TOPIC_ALIAS_INVALID
        disconnectReason2.reason() == null
        disconnectReason2.serverReference() == null
  }

  def "should response that no any information about topic name when it cannot be resolved by topic alias"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new PublishMqttInMessageHandler(
            publishReceivingService,
            defaultMessageOutFactoryService,
            defaultTopicService,
            disabledAclService,
            defaultPublishDataStorage)
        def expectedMessageId = 15
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
    when:
        def publishMessage = new PublishMqttInMessage(0b0110_0011 as byte) {{
          messageId = expectedMessageId
          payload = [1, 2, 3, 4, 5]
          topicAlias = 5
        }}
        messageHandler.processValidMessage(mqttConnection, publishMessage)
    then:
        def disconnectReason = mqttUser.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnectReason.reasonCode() == DisconnectReasonCode.PROTOCOL_ERROR
        disconnectReason.reason() == MqttProtocolErrors.NO_ANY_TOPIC_NANE
        disconnectReason.serverReference() == null
  }

  def "should response that topic name is invalid"() {
    given:
        def mqttConnection = mockedExternalConnection(MqttVersion.MQTT_5)
        def messageHandler = new PublishMqttInMessageHandler(
            publishReceivingService,
            defaultMessageOutFactoryService,
            defaultTopicService,
            disabledAclService,
            defaultPublishDataStorage)
        def expectedMessageId = 15
        def mqttUser = mqttConnection.user() as TestExternalNetworkMqttUser
    when:
        def publishMessage = new PublishMqttInMessage(0b0110_0011 as byte) {{
          messageId = expectedMessageId
          payload = [1, 2, 3, 4, 5]
          rawTopicName = "topic###"
        }}
        messageHandler.processValidMessage(mqttConnection, publishMessage)
    then:
        def disconnectReason = mqttUser.nextSentMessage(DisconnectMqtt5OutMessage)
        disconnectReason.reasonCode() == DisconnectReasonCode.TOPIC_NAME_INVALID
        disconnectReason.reason() == null
        disconnectReason.serverReference() == null
  }
}
