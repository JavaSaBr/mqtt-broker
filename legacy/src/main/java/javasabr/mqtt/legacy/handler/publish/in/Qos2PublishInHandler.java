package javasabr.mqtt.legacy.handler.publish.in;

import javasabr.mqtt.legacy.handler.publish.out.PublishOutHandler;
import javasabr.mqtt.model.ActionResult;
import javasabr.mqtt.legacy.network.MqttSession;
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode;
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode;
import javasabr.mqtt.legacy.network.MqttClient;
import javasabr.mqtt.legacy.network.packet.HasPacketId;
import javasabr.mqtt.legacy.network.packet.in.PublishInPacket;
import javasabr.mqtt.legacy.network.packet.in.PublishReleaseInPacket;
import javasabr.mqtt.legacy.service.SubscriptionService;

public class Qos2PublishInHandler extends AbstractPublishInHandler implements MqttSession.PendingPacketHandler {

  public Qos2PublishInHandler(SubscriptionService subscriptionService, PublishOutHandler[] publishOutHandlers) {
    super(subscriptionService, publishOutHandlers);
  }

  @Override
  public void handle(MqttClient client, PublishInPacket packet) {

    var session = client.getSession();

    // it means this client was already closed
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

    super.handle(client, packet);
  }

  @Override
  protected void handleResult(MqttClient client, PublishInPacket packet, ActionResult result) {

    // because it was checked
    final MqttSession session = client.getSession();

    // it means this client was already closed
    if (session == null) {
      return;
    }

    PublishReceivedReasonCode reasonCode;

    switch (result) {
      case EMPTY:
        reasonCode = PublishReceivedReasonCode.NO_MATCHING_SUBSCRIBERS;
        break;
      case SUCCESS:
        reasonCode = PublishReceivedReasonCode.SUCCESS;
        break;
      default:
        reasonCode = PublishReceivedReasonCode.UNSPECIFIED_ERROR;
        break;
    }

    session.registerInPublish(packet, this, packet.getPacketId());

    client.send(client
        .getPacketOutFactory()
        .newPublishReceived(packet.getPacketId(), reasonCode));
  }

  @Override
  public boolean handleResponse(MqttClient client, HasPacketId response) {

    if (!(response instanceof PublishReleaseInPacket)) {
      throw new IllegalStateException("Unexpected response " + response);
    }

    var packetOutFactory = client.getPacketOutFactory();
    client.send(packetOutFactory.newPublishCompleted(response.getPacketId(), PublishCompletedReasonCode.SUCCESS));

    return true;
  }
}
