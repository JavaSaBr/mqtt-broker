package javasabr.mqtt.service.publish.handler;

import javasabr.mqtt.model.ActionResult;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.network.packet.in.PublishInPacket;

public interface MqttPublishOutMessageHandler {

  QoS qos();

  ActionResult handle(PublishInPacket packet, SingleSubscriber subscriber);
}
