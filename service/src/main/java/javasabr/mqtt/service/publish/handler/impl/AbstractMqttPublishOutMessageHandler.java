package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.packet.in.PublishInPacket;
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

  @Override
  public PublishHandlingResult handle(PublishInPacket packet, SingleSubscriber subscriber) {
    MqttClient mqttClient = subscriptionService.resolveClient(subscriber);
    if (!expectedClient.isInstance(mqttClient)) {
      log.warning(mqttClient, "Accepted not expected client:[%s]"::formatted);
      return PublishHandlingResult.NOT_EXPECTED_CLIENT;
    }
    return handleImpl(packet, expectedClient.cast(mqttClient));
  }

  protected abstract PublishHandlingResult handleImpl(PublishInPacket packet, C client) ;

  protected void startDelivering(
      MqttClient client,
      PublishInPacket packet,
      int messageId,
      boolean duplicate) {
    var packetOutFactory = client.packetOutFactory();
    client.send(packetOutFactory.newPublish(
        messageId,
        qos(),
        packet.isRetained(),
        duplicate,
        packet
            .getTopicName()
            .toString(),
        MqttProperties.TOPIC_ALIAS_NOT_SET,
        packet.getPayload(),
        packet.isPayloadFormatIndicator(),
        packet.getResponseTopic(),
        packet.getCorrelationData(),
        packet.userProperties()));
  }
}
