package javasabr.mqtt.legacy.handler.publish.in;

import javasabr.mqtt.legacy.handler.publish.out.PublishOutHandler;
import javasabr.mqtt.model.ActionResult;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.packet.in.PublishInPacket;
import javasabr.mqtt.legacy.service.SubscriptionService;
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
