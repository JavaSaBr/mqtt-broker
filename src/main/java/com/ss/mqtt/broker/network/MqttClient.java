package com.ss.mqtt.broker.network;

import com.ss.mqtt.broker.model.ConnectReturnCode;
import com.ss.mqtt.broker.model.MqttVersion;
import com.ss.mqtt.broker.network.packet.out.ConnectAckOutPacket;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MqttClient {

    private final @Getter MqttConnection connection;

    public MqttClient(@NotNull MqttConnection connection) {
        this.connection = connection;
    }

    public void reject(@NotNull ConnectReturnCode returnCode) {
        connection.send(new ConnectAckOutPacket(returnCode, false));
    }

    public void onConnected(
        @NotNull MqttVersion mqttVersion,
        @NotNull String clientId,
        @Nullable String username,
        @Nullable String password,
        @Nullable String willTopic,
        @Nullable byte[] willMessage,
        int keepAlive,
        boolean willRetain,
        int willQos,
        boolean cleanStart
    ) {
        connection.send(new ConnectAckOutPacket(ConnectReturnCode.ACCEPTED, true));
    }
}
