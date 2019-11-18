package com.ss.mqtt.broker.network.client;

import com.ss.mqtt.broker.config.MqttConnectionConfig;
import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.model.reason.code.ConnectAckReasonCode;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.factory.packet.out.MqttPacketOutFactory;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface MqttClient {

    interface UnsafeMqttClient extends MqttClient {

        @NotNull MqttConnection getConnection();

        void handle(@NotNull MqttReadablePacket packet);

        void configure(
            long sessionExpiryInterval,
            int receiveMax,
            int maximumPacketSize,
            int topicAliasMaximum,
            int keepAlive,
            boolean requestResponseInformation,
            boolean requestProblemInformation
        );

        void setClientId(@NotNull String clientId);

        void setSession(@NotNull MqttSession session);

        void reject(@NotNull ConnectAckReasonCode reasonCode);

        @NotNull Mono<?> release();
    }

    @NotNull MqttPacketOutFactory getPacketOutFactory();
    @NotNull MqttConnectionConfig getConnectionConfig();

    @NotNull String getClientId();
    @NotNull MqttSession getSession();

    int getKeepAlive();
    int getMaximumPacketSize();
    int getReceiveMax();
    int getTopicAliasMaximum();

    long getSessionExpiryInterval();

    void send(@NotNull MqttWritablePacket packet);
}
