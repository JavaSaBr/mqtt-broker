package javasabr.mqtt.model.subscription

import com.fasterxml.jackson.annotation.JsonValue

record TestSubscriptionOwner(String id) implements SubscriptionOwner {

  @JsonValue
  @Override
  String toString() {
    return id
  }
}