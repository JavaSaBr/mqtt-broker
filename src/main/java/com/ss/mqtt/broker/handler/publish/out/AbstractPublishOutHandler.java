package com.ss.mqtt.broker.handler.publish.out;

import com.ss.mqtt.broker.model.ActionResult;
import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.model.SingleSubscriber;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;

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
