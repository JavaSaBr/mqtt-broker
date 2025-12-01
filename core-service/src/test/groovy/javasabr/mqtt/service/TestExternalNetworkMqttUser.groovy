package javasabr.mqtt.service

import javasabr.mqtt.model.message.SendableMqttMessage
import javasabr.mqtt.network.MqttConnection
import javasabr.mqtt.network.handler.NetworkMqttUserReleaseHandler
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser
import javasabr.mqtt.network.message.out.MqttOutMessage
import javasabr.rlib.collections.array.MutableArray

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class TestExternalNetworkMqttUser extends ExternalNetworkMqttUser {

  private static final Executor DELAYED_EXECUTOR = CompletableFuture.delayedExecutor(5000, TimeUnit.MILLISECONDS)

  private final MutableArray<MqttOutMessage> sentMessages
  private boolean returnCompletedFeatures = true;

  TestExternalNetworkMqttUser(MqttConnection connection, NetworkMqttUserReleaseHandler releaseHandler) {
    super(connection, releaseHandler)
    this.sentMessages = MutableArray.ofType(MqttOutMessage)
  }

  void returnCompletedFeatures(boolean returnCompletedFeatures) {
    this.returnCompletedFeatures = returnCompletedFeatures;
  }

  @Override
  void sendAsync(SendableMqttMessage message) {
    sendAsync((MqttOutMessage) message)
  }

  @Override
  void sendAsync(MqttOutMessage message) {
    sentMessages.add(message)
  }

  @Override
  CompletionStage<Boolean> send(SendableMqttMessage message) {
    return send((MqttOutMessage) message)
  }
  
  @Override
  CompletableFuture<Boolean> send(MqttOutMessage message) {
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

  boolean isEmpty() {
    return sentMessages.isEmpty()
  }
}
