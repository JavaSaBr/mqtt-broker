package javasabr.mqtt.model.session;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.message.TrackableMqttMessage;

public interface TrackableMessageCallback {

  /**
   * @return true if this handler should be de-register
   */
  boolean accept(MqttUser owner, Object session, TrackableMqttMessage message);
}
