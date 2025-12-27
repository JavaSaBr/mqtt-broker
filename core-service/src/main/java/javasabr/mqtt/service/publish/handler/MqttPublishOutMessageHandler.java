package javasabr.mqtt.service.publish.handler;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.subscriber.SingleSubscriber;

public interface MqttPublishOutMessageHandler {

  QoS qos();

  void handle(Publish publish, MqttUser user);
}
