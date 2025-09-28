package javasabr.mqtt.service.handler.publish.in;

import javasabr.mqtt.service.handler.publish.out.PublishOutHandler;
import javasabr.mqtt.model.ActionResult;
import javasabr.mqtt.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.packet.in.PublishInPacket;
import javasabr.mqtt.service.SubscriptionService;

public class Qos1PublishInHandler extends AbstractPublishInHandler {

  public Qos1PublishInHandler(SubscriptionService subscriptionService, PublishOutHandler[] publishOutHandlers) {
    super(subscriptionService, publishOutHandlers);
  }

  @Override
  public void handle(MqttClient client, PublishInPacket packet) {

    var session = client.getSession();

    // it means this client was already closed
    if (session == null) {
      return;
    }

    super.handle(client, packet);
  }

  @Override
  protected void handleResult(MqttClient client, PublishInPacket packet, ActionResult result) {

    PublishAckReasonCode reasonCode;

    switch (result) {
      case EMPTY:
        reasonCode = PublishAckReasonCode.NO_MATCHING_SUBSCRIBERS;
        break;
      case SUCCESS:
        reasonCode = PublishAckReasonCode.SUCCESS;
        break;
      default:
        reasonCode = PublishAckReasonCode.UNSPECIFIED_ERROR;
        break;
    }

    client.send(client
        .getPacketOutFactory()
        .newPublishAck(packet.getPacketId(), reasonCode));
  }
}
