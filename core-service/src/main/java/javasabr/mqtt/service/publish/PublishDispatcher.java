package javasabr.mqtt.service.publish;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.subscription.Subscription;

public interface PublishDispatcher {

  void dispatchToSubscriber(IncomingPublish publish, MqttUser user, Subscription subscription);
}
