package javasabr.mqtt.network.session;

import javasabr.mqtt.model.session.MqttSession;
import javasabr.mqtt.network.user.NetworkMqttUser;

public interface NetworkMqttSession extends MqttSession {

  /**
   * @return the count of resent publishes
   */
  int resendNotConfirmedPublishesTo(NetworkMqttUser user);
}
