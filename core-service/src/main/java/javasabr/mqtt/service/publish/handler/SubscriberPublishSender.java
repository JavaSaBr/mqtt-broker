package javasabr.mqtt.service.publish.handler;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publish.Publish;

public interface SubscriberPublishSender {

  QoS qos();

  void sendToSubscriber(Publish publish, MqttUser user);
}
