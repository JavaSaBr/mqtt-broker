package javasabr.mqtt.model.subscription

import com.fasterxml.jackson.annotation.JsonValue
import javasabr.mqtt.model.MqttUser

record TestMqttUser(String id) implements MqttUser {

  @JsonValue
  @Override
  String toString() {
    return id
  }
}
