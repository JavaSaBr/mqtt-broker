package javasabr.mqtt.service.handler.publish.out;

import javasabr.mqtt.model.ActionResult;
import javasabr.mqtt.model.subscriber.Subscriber;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.MqttSession;
import javasabr.mqtt.network.packet.in.PublishInPacket;

public abstract class PersistentPublishOutHandler extends AbstractPublishOutHandler implements
    MqttSession.PendingMessageHandler {

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
