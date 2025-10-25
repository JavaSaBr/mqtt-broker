package javasabr.mqtt.service.publish.handler;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;

public interface MqttPublishOutMessageHandler {

  QoS qos();

  PublishHandlingResult handle(PublishMqttInMessage packet, SingleSubscriber subscriber);
}
