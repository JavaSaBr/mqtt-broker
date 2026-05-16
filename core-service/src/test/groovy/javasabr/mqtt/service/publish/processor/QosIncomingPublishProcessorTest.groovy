package javasabr.mqtt.service.publish.processor

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.PayloadFormat
import javasabr.mqtt.model.QoS
import javasabr.mqtt.model.publish.IncomingPublish
import javasabr.mqtt.model.publish.PublishData
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.network.message.in.MqttInMessage
import javasabr.mqtt.service.IntegrationServiceSpecification
import javasabr.rlib.collections.array.IntArray

abstract class QosIncomingPublishProcessorTest extends IntegrationServiceSpecification {
  
  static {
    //LoggerManager.enable(AbstractNetworkMqttUser.class, LoggerLevel.DEBUG)
    //LoggerManager.enable(AbstractIncomingPublishProcessor.class, LoggerLevel.DEBUG)
    //LoggerManager.enable(TrackableIncomingPublishProcessor.class, LoggerLevel.DEBUG)
    //LoggerManager.enable(Qos0IncomingPublishProcessor.class, LoggerLevel.DEBUG)
    //LoggerManager.enable(Qos1IncomingPublishProcessor.class, LoggerLevel.DEBUG)
    //LoggerManager.enable(Qos2IncomingPublishProcessor.class, LoggerLevel.DEBUG)
    //LoggerManager.enable(InMemoryIncomingPublishStorage, LoggerLevel.DEBUG)
  }

  protected def preparePublish(QoS qos, TopicName topicName, byte[] payload) {
    PublishData data = preparePublishData(payload)
    return defaultIncomingPublishStorage.store(
        UUID.randomUUID(),
        MqttProperties.MESSAGE_ID_IS_NOT_SET,
        qos,
        topicName,
        null,
        data,
        false,
        false,
        IntArray.empty(),
        60_000,
        MqttProperties.TOPIC_ALIAS_MAX_IS_NOT_SET,
        MqttInMessage.EMPTY_USER_PROPERTIES)
  }

  protected IncomingPublish preparePublish(
      int messageId, 
      QoS qos, 
      TopicName topicName, 
      byte[] payload) {
    PublishData data = preparePublishData(payload)
    return defaultIncomingPublishStorage.store(
        UUID.randomUUID(),
        messageId,
        qos,
        topicName,
        null,
        data,
        false,
        false,
        IntArray.empty(),
        60_000,
        MqttProperties.TOPIC_ALIAS_MAX_IS_NOT_SET,
        MqttInMessage.EMPTY_USER_PROPERTIES)
  }

  protected IncomingPublish prepareDuplicatedPublish(
      int messageId,
      QoS qos,
      TopicName topicName,
      byte[] payload) {
    PublishData data = preparePublishData(payload)
    return defaultIncomingPublishStorage.store(
        UUID.randomUUID(),
        messageId,
        qos,
        topicName,
        null,
        data,
        true,
        false,
        IntArray.empty(),
        60_000,
        MqttProperties.TOPIC_ALIAS_MAX_IS_NOT_SET,
        MqttInMessage.EMPTY_USER_PROPERTIES)
  }

  protected PublishData preparePublishData(byte[] payload) {
    return defaultPublishDataStorage.store(
        UUID.randomUUID(),
        null,
        PayloadFormat.BINARY,
        payload,
        null)
  }
}
