package javasabr.mqtt.model.session;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.publishing.Publish;

public interface PublishRetryer {

  void retry(MqttUser owner, Object session, Publish publish);
}
