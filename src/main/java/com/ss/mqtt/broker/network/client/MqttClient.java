package com.ss.mqtt.broker.network.client;

import com.ss.mqtt.broker.config.MqttConnectionConfig;
import com.ss.mqtt.broker.factory.packet.out.MqttPacketOutFactory;
import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.model.reason.code.ConnectAckReasonCode;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

public interface MqttClient {

  interface UnsafeMqttClient extends MqttClient {

    MqttConnection getConnection();

    void handle(MqttReadablePacket packet);

    void configure(
        long sessionExpiryInterval,
        int receiveMax,
        int maximumPacketSize,
        int topicAliasMaximum,
        int keepAlive,
        boolean requestResponseInformation,
        boolean requestProblemInformation);

    void setClientId(String clientId);

    void setSession(@Nullable MqttSession session);

    void reject(ConnectAckReasonCode reasonCode);

    Mono<?> release();
  }

  MqttPacketOutFactory getPacketOutFactory();

  MqttConnectionConfig getConnectionConfig();

  String getClientId();

  @Nullable
  MqttSession getSession();

  int getKeepAlive();

  int getMaximumPacketSize();

  int getReceiveMax();

  int getTopicAliasMaximum();

  long getSessionExpiryInterval();

  void send(MqttWritablePacket packet);

  CompletableFuture<Boolean> sendWithFeedback(MqttWritablePacket packet);
}
