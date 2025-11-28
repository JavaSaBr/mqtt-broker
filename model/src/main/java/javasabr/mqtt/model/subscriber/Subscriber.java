package javasabr.mqtt.model.subscriber;

import javasabr.mqtt.model.MqttUser;

public sealed interface Subscriber permits SingleSubscriber, SharedSubscriber {

  /**
   * Resolves the owner of a subscription to send a publishing.
   */
  default MqttUser resolveUser() {
    return resolveSingle().user();
  }

  /**
   * Resolves the owner of a subscription to send a publishing.
   */
  SingleSubscriber resolveSingle();
}
