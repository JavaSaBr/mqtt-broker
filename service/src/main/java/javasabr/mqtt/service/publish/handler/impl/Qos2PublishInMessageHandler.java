package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode;
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.MqttSession;
import javasabr.mqtt.network.MqttSession.PendingMessageHandler;
import javasabr.mqtt.network.client.ExternalMqttClient;
import javasabr.mqtt.network.packet.HasPacketId;
import javasabr.mqtt.network.packet.in.PublishInPacket;
import javasabr.mqtt.network.packet.in.PublishReleaseInPacket;
import javasabr.mqtt.service.PublishDeliveringService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.message.handler.PublishHandlingResult;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Qos2PublishInMessageHandler extends Qos0PublishInMessageHandler {

  PendingMessageHandler pendingMessageHandler;

  public Qos2PublishInMessageHandler(
      SubscriptionService subscriptionService,
      PublishDeliveringService publishDeliveringService) {
    super(subscriptionService, publishDeliveringService);
    this.pendingMessageHandler = this::processPublishRelease;
  }

  @Override
  public QoS qos() {
    return QoS.EXACTLY_ONCE;
  }

  @Override
  protected void handleImpl(ExternalMqttClient client, PublishInPacket packet) {
    MqttSession session = client.session();
    if (session == null) {
      return;
    }
    // if this packet is re-try from client
    if (packet.isDuplicate()) {
      // if this packet was accepted before then we can skip it
      if (session.hasInPending(packet.getPacketId())) {
        return;
      }
    }
    super.handleImpl(client, packet);
  }

  @Override
  protected void handleInvalidTopic(ExternalMqttClient client, int messageId, TopicName topicName) {
    super.handleInvalidTopic(client, messageId, topicName);
    client.send(client
        .packetOutFactory()
        .newPublishReceived(messageId, PublishReceivedReasonCode.TOPIC_NAME_INVALID));
  }

  @Override
  protected void handleEmptySubscriptions(ExternalMqttClient client, int messageId, TopicName topicName) {
    super.handleEmptySubscriptions(client, messageId, topicName);
    client.send(client
        .packetOutFactory()
        .newPublishReceived(messageId, PublishReceivedReasonCode.NO_MATCHING_SUBSCRIBERS));
  }

  @Override
  protected void handleError(ExternalMqttClient client, int messageId, PublishHandlingResult handlingResult) {
    super.handleError(client, messageId, handlingResult);
    client.send(client
        .packetOutFactory()
        .newPublishReceived(messageId, handlingResult.receivedReasonCode()));
  }

  @Override
  protected void startDelivering(ExternalMqttClient client, PublishInPacket packet, SingleSubscriber subscriber) {

    MqttSession session = client.session();
    if (session == null) {
      return;
    }

    session.registerInPublish(packet, pendingMessageHandler, packet.getPacketId());
    super.startDelivering(client, packet, subscriber);
    client.send(client
        .packetOutFactory()
        .newPublishReceived(packet.getPacketId(), PublishReceivedReasonCode.SUCCESS));
  }

  private boolean processPublishRelease(MqttClient client, HasPacketId response) {
    if (!(response instanceof PublishReleaseInPacket)) {
      throw new IllegalStateException("Unexpected response " + response);
    }

    var packetOutFactory = client.packetOutFactory();
    client.send(packetOutFactory.newPublishCompleted(response.packetId(), PublishCompletedReasonCode.SUCCESS));
    return true;
  }
}
