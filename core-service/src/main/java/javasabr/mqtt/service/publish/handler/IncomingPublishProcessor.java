package javasabr.mqtt.service.publish.handler;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.publish.Publish;
import javasabr.mqtt.network.user.NetworkMqttUser;

public interface IncomingPublishProcessor {

  QoS qos();

  void process(NetworkMqttUser user, Publish publish);
}
