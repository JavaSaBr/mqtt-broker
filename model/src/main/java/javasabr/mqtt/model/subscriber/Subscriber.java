package javasabr.mqtt.model.subscriber;

import javasabr.mqtt.model.subscribtion.SubscriptionOwner;

public sealed interface Subscriber permits SingleSubscriber, SharedSubscriber {

  /**
   * Resolves the owner of a subscription to send a publishing.
   */
  default SubscriptionOwner resolveOwner() {
    return resolveSingle().owner();
  }

  /**
   * Resolves the owner of a subscription to send a publishing.
   */
  SingleSubscriber resolveSingle();
}
