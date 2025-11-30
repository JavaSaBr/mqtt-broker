package javasabr.mqtt.model.subscription

import com.fasterxml.jackson.annotation.JsonValue
import javasabr.mqtt.model.MqttUser
import javasabr.mqtt.model.message.SendableMqttMessage
import javasabr.mqtt.model.session.MqttSession
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

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

  @Override
  void sendAsync(SendableMqttMessage message) {
  }

  @Override
  CompletionStage<Boolean> send(SendableMqttMessage message) {
    return CompletableFuture.completedFuture(true)
  }
}
