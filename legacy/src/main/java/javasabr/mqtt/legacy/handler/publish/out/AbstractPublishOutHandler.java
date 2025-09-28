package javasabr.mqtt.legacy.handler.publish.out;

import javasabr.mqtt.model.ActionResult;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.network.MqttSession;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.model.subscriber.Subscriber;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.packet.in.PublishInPacket;

abstract class AbstractPublishOutHandler implements PublishOutHandler {

  @Override
  public ActionResult handle(PublishInPacket packet, SingleSubscriber subscriber) {

    MqttClient client = (MqttClient) subscriber.getUser();
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
        MqttProperties.TOPIC_ALIAS_NOT_SET,
        packet.getPayload(),
        packet.isPayloadFormatIndicator(),
        packet.getResponseTopic(),
        packet.getCorrelationData(),
        packet.getUserProperties()));
  }
}
