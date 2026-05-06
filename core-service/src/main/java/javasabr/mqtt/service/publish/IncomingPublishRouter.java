package javasabr.mqtt.service.publish;

import javasabr.mqtt.model.publish.Publish;
import javasabr.mqtt.network.user.NetworkMqttUser;

public interface IncomingPublishRouter {

  void route(NetworkMqttUser user, Publish publish);
}
