package javasabr.mqtt.model.subscription

import com.fasterxml.jackson.annotation.JsonValue
import javasabr.mqtt.model.subscribtion.SubscriptionOwner

record TestSubscriptionOwner(String id) implements SubscriptionOwner {

  @JsonValue
  @Override
  String toString() {
    return id
  }
}