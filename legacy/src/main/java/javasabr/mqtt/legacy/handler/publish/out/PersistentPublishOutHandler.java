package javasabr.mqtt.legacy.handler.publish.out;

import javasabr.mqtt.legacy.model.ActionResult;
import javasabr.mqtt.legacy.model.MqttSession;
import javasabr.mqtt.legacy.model.Subscriber;
import javasabr.mqtt.legacy.network.client.MqttClient;
import javasabr.mqtt.legacy.network.packet.in.PublishInPacket;

public abstract class PersistentPublishOutHandler extends AbstractPublishOutHandler implements
    MqttSession.PendingPacketHandler {

  @Override
  protected ActionResult handleImpl(
      PublishInPacket packet,
      Subscriber subscriber,
      MqttClient client,
      MqttSession session) {
    // generate new uniq packet id per client
    var packetId = session.nextPacketId();

    // register waiting async response
    session.registerOutPublish(packet, this, packetId);

    // send publish
    sendPublish(client, packet, packetId, false);

    return ActionResult.SUCCESS;
  }

  @Override
  public void resend(MqttClient client, PublishInPacket packet, int packetId) {
    sendPublish(client, packet, packetId, true);
  }
}
