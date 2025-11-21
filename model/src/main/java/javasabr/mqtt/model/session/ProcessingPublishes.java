package javasabr.mqtt.model.session;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.TrackableMessage;
import javasabr.mqtt.model.publishing.Publish;

public interface ProcessingPublishes {

  void register(Publish publish, TrackableMessageCallback callback, PublishRetryer retryer);

  /**
   * @return true if was found some callback for this message
   */
  boolean apply(MqttUser user, TrackableMessage message);

  /**
   * @return true if was found some callback for this message
   */
  boolean remove(TrackableMessage message);
}
