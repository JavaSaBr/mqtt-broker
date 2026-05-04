package javasabr.mqtt.service.publish.sender;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publish.IncomingPublish;

public interface SubscriberPublishSender {

  QoS qos();

  void sendToSubscriber(IncomingPublish publish, MqttUser user);
}
