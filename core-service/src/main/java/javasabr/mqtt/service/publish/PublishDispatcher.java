package javasabr.mqtt.service.publish;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.publish.Publish;
import javasabr.mqtt.model.subscription.Subscription;

public interface PublishDispatcher {

  void dispatchToSubscriber(Publish publish, MqttUser user, Subscription subscription);
}
