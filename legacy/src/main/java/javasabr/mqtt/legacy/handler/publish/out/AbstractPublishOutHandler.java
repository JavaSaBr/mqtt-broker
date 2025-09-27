package javasabr.mqtt.legacy.handler.publish.out;

import javasabr.mqtt.legacy.model.ActionResult;
import javasabr.mqtt.legacy.model.MqttPropertyConstants;
import javasabr.mqtt.legacy.model.MqttSession;
import javasabr.mqtt.legacy.model.QoS;
import javasabr.mqtt.legacy.model.SingleSubscriber;
import javasabr.mqtt.legacy.model.Subscriber;
import javasabr.mqtt.legacy.network.client.MqttClient;
import javasabr.mqtt.legacy.network.packet.in.PublishInPacket;

abstract class AbstractPublishOutHandler implements PublishOutHandler {

  @Override
  public ActionResult handle(PublishInPacket packet, SingleSubscriber subscriber) {

    var client = subscriber.getMqttClient();
    var session = client.getSession();

    // if session is null it means this client was already closed
    if (session != null) {
      return handleImpl(packet, subscriber, client, session);
    } else {
      return ActionResult.EMPTY;
    }
  }

  protected abstract ActionResult handleImpl(
      PublishInPacket packet,
      Subscriber subscriber,
      MqttClient client,
      MqttSession session);

  protected abstract QoS getQoS();

  void sendPublish(MqttClient client, PublishInPacket packet, int packetId, boolean duplicate) {

    var packetOutFactory = client.getPacketOutFactory();
    client.send(packetOutFactory.newPublish(
        packetId,
        getQoS(),
        packet.isRetained(),
        duplicate,
        packet
            .getTopicName()
            .toString(),
        MqttPropertyConstants.TOPIC_ALIAS_NOT_SET,
        packet.getPayload(),
        packet.isPayloadFormatIndicator(),
        packet.getResponseTopic(),
        packet.getCorrelationData(),
        packet.getUserProperties()));
  }
}
