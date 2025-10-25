package javasabr.mqtt.service.publish.handler.impl;

import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.MqttSession;
import javasabr.mqtt.network.MqttSession.PendingMessageHandler;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.HasMessageId;
import javasabr.mqtt.network.packet.in.PublishInPacket;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.publish.handler.PublishHandlingResult;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class PersistedMqttPublishOutMessageHandler extends AbstractMqttPublishOutMessageHandler<ExternalMqttClient> {

  PendingMessageHandler pendingMessageHandler;

  protected PersistedMqttPublishOutMessageHandler(
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalMqttClient.class, subscriptionService, messageOutFactoryService);
    this.pendingMessageHandler = new PendingMessageHandler() {
      @Override
      public boolean handleResponse(MqttClient client, HasMessageId response) {
        return handleReceivedResponse(client, response);
      }
      @Override
      public void resend(MqttClient client, PublishInPacket packet, int packetId) {
        tryToDeliverAgain(client, packet, packetId);
      }
    };
  }

  @Override
  protected PublishHandlingResult handleImpl(PublishInPacket packet, ExternalMqttClient client) {

    MqttSession session = client.session();
    if (session == null) {
      return PublishHandlingResult.SKIPPED;
    }

    // generate new uniq packet id per client
    int packetId = session.nextPacketId();
    // register waiting async response
    session.registerOutPublish(packet, pendingMessageHandler, packetId);

    // send publish
    startDelivering(client, packet, packetId, false);
    return PublishHandlingResult.SUCCESS;
  }

  protected boolean handleReceivedResponse(MqttClient client, HasMessageId response) {
    return false;
  }

  protected void tryToDeliverAgain(MqttClient client, PublishInPacket packet, int messageId) {
    startDelivering(client, packet, messageId, true);
  }
}
