package javasabr.mqtt.service.publish.processor;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.network.user.NetworkMqttUser;

public interface IncomingPublishProcessor {

  QoS qos();

  void process(NetworkMqttUser user, IncomingPublish publish);
}
