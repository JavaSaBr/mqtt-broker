package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import javasabr.mqtt.network.packet.out.PublishOutPacket;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.handler.MqttPublishOutMessageHandler;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@CustomLog
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractMqttPublishOutMessageHandler<C extends MqttClient>
    implements MqttPublishOutMessageHandler {

  Class<C> expectedClient;
  SubscriptionService subscriptionService;
  MessageOutFactoryService messageOutFactoryService;

  @Override
  public PublishHandlingResult handle(PublishMqttInMessage packet, SingleSubscriber subscriber) {
    MqttClient mqttClient = subscriptionService.resolveClient(subscriber);
    if (!expectedClient.isInstance(mqttClient)) {
      log.warning(mqttClient, "Accepted not expected client:[%s]"::formatted);
      return PublishHandlingResult.NOT_EXPECTED_CLIENT;
    }
    return handleImpl(packet, expectedClient.cast(mqttClient));
  }

  protected abstract PublishHandlingResult handleImpl(PublishMqttInMessage packet, C client) ;

  protected void startDelivering(
      MqttClient client,
      PublishMqttInMessage packet,
      int messageId,
      boolean duplicate) {
    PublishOutPacket publish = messageOutFactoryService
        .resolveFactory(client)
        .newPublish(
            messageId,
            qos(),
            packet.retained(),
            duplicate,
            packet
                .topicName()
                .toString(),
            MqttProperties.TOPIC_ALIAS_NOT_SET,
            packet.payload(),
            packet.payloadFormatIndicator(),
            packet.responseTopic(),
            packet.correlationData(),
            packet.userProperties());
    client.send(publish);
  }
}
