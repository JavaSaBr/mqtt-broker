package javasabr.mqtt.service

import javasabr.mqtt.network.MqttConnection
import javasabr.mqtt.network.handler.MqttClientReleaseHandler
import javasabr.mqtt.network.impl.ExternalMqttClient
import javasabr.mqtt.network.message.out.MqttOutMessage
import javasabr.rlib.collections.array.MutableArray

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class TestExternalMqttClient extends ExternalMqttClient {

  private static final Executor DELAYED_EXECUTOR = CompletableFuture.delayedExecutor(5000, TimeUnit.MILLISECONDS)

  private final MutableArray<MqttOutMessage> sentMessages
  private boolean returnCompletedFeatures = true;

  TestExternalMqttClient(MqttConnection connection, MqttClientReleaseHandler releaseHandler) {
    super(connection, releaseHandler)
    this.sentMessages = MutableArray.ofType(MqttOutMessage)
  }

  void returnCompletedFeatures(boolean returnCompletedFeatures) {
    this.returnCompletedFeatures = returnCompletedFeatures;
  }

  @Override
  void send(MqttOutMessage message) {
    sentMessages.add(message)
  }

  @Override
  CompletableFuture<Boolean> sendWithFeedback(MqttOutMessage message) {
    sentMessages.add(message)
    if (!returnCompletedFeatures) {
      return CompletableFuture.supplyAsync({ true }, DELAYED_EXECUTOR);
    }
    return CompletableFuture.completedFuture(true)
  }

  @Override
  CompletableFuture<Boolean> closeWithReason(MqttOutMessage message) {
    sentMessages.add(message)
    if (!returnCompletedFeatures) {
      return CompletableFuture.supplyAsync({ true }, DELAYED_EXECUTOR);
    }
    return CompletableFuture.completedFuture(true)
  }

  <M extends MqttOutMessage> M nextSentMessage(Class<M> type) {
    return type.cast(sentMessages.remove(0))
  }
}
