package javasabr.mqtt.model.session;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.publish.Publish;

public interface PublishRetryer {

  PublishRetryer NO_OPS = (owner, session, publish) -> {};

  void retry(MqttUser user, MqttSession session, Publish publish);
}
