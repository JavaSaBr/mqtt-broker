package javasabr.mqtt.service

import javasabr.mqtt.network.MqttConnection
import javasabr.mqtt.network.handler.MqttClientReleaseHandler
import javasabr.mqtt.network.impl.ExternalMqttClient
import javasabr.mqtt.network.message.out.MqttOutMessage
import javasabr.rlib.collections.array.MutableArray

import java.util.concurrent.CompletableFuture

class TestExternalMqttClient extends ExternalMqttClient {

  private final MutableArray<MqttOutMessage> sentMessages

  TestExternalMqttClient(MqttConnection connection, MqttClientReleaseHandler releaseHandler) {
    super(connection, releaseHandler)
    this.sentMessages = MutableArray.ofType(MqttOutMessage)
  }

  @Override
  void send(MqttOutMessage message) {
    sentMessages.add(message)
  }

  @Override
  CompletableFuture<Boolean> sendWithFeedback(MqttOutMessage message) {
    sentMessages.add(message)
    return CompletableFuture.completedFuture(true)
  }

  @Override
  CompletableFuture<Boolean> closeWithReason(MqttOutMessage message) {
    sentMessages.add(message)
    return CompletableFuture.completedFuture(true)
  }

  <M extends MqttOutMessage> M nextSentMessage(Class<M> type) {
    return type.cast(sentMessages.remove(0))
  }
}
