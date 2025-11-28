package javasabr.mqtt.model.subscription

import com.fasterxml.jackson.annotation.JsonValue
import javasabr.mqtt.model.MqttUser
import javasabr.mqtt.model.message.SendableMqttMessage
import javasabr.mqtt.model.session.MqttSession

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

record TestMqttUser(String id) implements MqttUser {

  @Override
  String clientId() {
    return id
  }

  @JsonValue
  @Override
  String toString() {
    return id
  }

  @Override
  String userName() {
    return null
  }

  @Override
  String ipAddress() {
    return "localhost"
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
