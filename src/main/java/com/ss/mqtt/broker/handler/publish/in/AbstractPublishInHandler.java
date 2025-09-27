package com.ss.mqtt.broker.handler.publish.in;

import com.ss.mqtt.broker.handler.publish.out.PublishOutHandler;
import com.ss.mqtt.broker.model.ActionResult;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.model.SingleSubscriber;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.mqtt.broker.service.SubscriptionService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class AbstractPublishInHandler implements PublishInHandler {

  protected final SubscriptionService subscriptionService;
  protected final PublishOutHandler[] publishOutHandlers;

  public void handle(MqttClient client, PublishInPacket packet) {
    handleResult(
        client,
        packet,
        subscriptionService.forEachTopicSubscriber(packet.getTopicName(), packet, this::sendToSubscriber));
  }

  private ActionResult sendToSubscriber(SingleSubscriber subscriber, PublishInPacket packet) {
    return publishOutHandler(subscriber.getQos()).handle(packet, subscriber);
  }

  private PublishOutHandler publishOutHandler(QoS qos) {
    return publishOutHandlers[qos.ordinal()];
  }

  protected void handleResult(MqttClient client, PublishInPacket packet, ActionResult result) {
    // nothing to do
  }
}
