package javasabr.mqtt.model.session;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publish.Publish;

public interface TrackableMessageCallback {

  /**
   * @return true if this handler should be de-register
   */
  boolean accept(MqttUser user, MqttSession session, TrackableMqttMessage message, Publish publish);
}
