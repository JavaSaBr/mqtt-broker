package javasabr.mqtt.service.publish;

import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.network.user.NetworkMqttUser;

public interface IncomingPublishRouter {

  void route(NetworkMqttUser user, IncomingPublish publish);
}
