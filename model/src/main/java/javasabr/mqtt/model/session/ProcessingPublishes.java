package javasabr.mqtt.model.session;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publish.Publish;

public interface ProcessingPublishes<P extends Publish> {

  void register(P publish, TrackableMessageCallback<P> callback, PublishRetryer retryer);

  /**
   * @return true if was found some callback for this message
   */
  boolean apply(MqttUser user, TrackableMqttMessage message);

  /**
   * @return true if was found some callback for this message
   */
  boolean remove(TrackableMqttMessage message);
  
  int size();
}
