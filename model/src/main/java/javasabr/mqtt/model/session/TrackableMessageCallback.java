package javasabr.mqtt.model.session;

import javasabr.mqtt.model.MqttUser;
import javasabr.mqtt.model.TrackableMessage;

public interface TrackableMessageCallback {

  /**
   * @return true if this handler should be de-register
   */
  boolean accept(MqttUser owner, Object session, TrackableMessage message);
}
