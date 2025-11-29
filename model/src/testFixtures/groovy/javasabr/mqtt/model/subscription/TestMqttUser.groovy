package javasabr.mqtt.model.subscription

import com.fasterxml.jackson.annotation.JsonValue
import javasabr.mqtt.model.MqttUser
import javasabr.mqtt.model.session.MqttSession

record TestMqttUser(String clientId, String userName, String ipAddress) implements MqttUser {

  TestMqttUser(String id) {
    this(id, null, "localhost")
  }

  @JsonValue
  @Override
  String toString() {
    return clientId
  }

  @Override
  MqttSession session() {
    return null
  }
}
