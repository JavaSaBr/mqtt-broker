package com.ss.mqtt.broker.handler.publish.out;

import com.ss.mqtt.broker.model.ActionResult;
import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;

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
