package com.ss.mqtt.broker.handler.publish.out;

import static com.ss.mqtt.broker.model.ActionResult.SUCCESS;

import com.ss.mqtt.broker.model.ActionResult;
import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.model.Subscriber;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;

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
