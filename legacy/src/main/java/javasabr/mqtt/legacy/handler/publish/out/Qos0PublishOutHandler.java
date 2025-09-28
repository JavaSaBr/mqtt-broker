package javasabr.mqtt.legacy.handler.publish.out;

import static javasabr.mqtt.model.ActionResult.SUCCESS;

import javasabr.mqtt.model.ActionResult;
import javasabr.mqtt.legacy.network.MqttSession;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.subscriber.Subscriber;
import javasabr.mqtt.legacy.network.MqttClient;
import javasabr.mqtt.legacy.network.packet.in.PublishInPacket;

public class Qos0PublishOutHandler extends AbstractPublishOutHandler {

  @Override
  protected QoS getQoS() {
    return QoS.AT_MOST_ONCE;
  }

  @Override
  protected ActionResult handleImpl(
      PublishInPacket packet,
      Subscriber subscriber,
      MqttClient client,
      MqttSession session) {
    sendPublish(client, packet, 0, false);
    return SUCCESS;
  }
}
