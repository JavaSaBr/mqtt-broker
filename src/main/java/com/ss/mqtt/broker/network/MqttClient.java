package com.ss.mqtt.broker.network;

import com.ss.mqtt.broker.model.ConnectReasonCode;
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket;
import com.ss.mqtt.broker.network.packet.out.ConnectAckOutPacket;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class MqttClient {

    private final @Getter
    MqttConnection connection;

    public MqttClient(@NotNull MqttConnection connection) {
        this.connection = connection;
    }

    public void reject(@NotNull ConnectReasonCode returnCode) {
        connection.send(new ConnectAckOutPacket(returnCode, false));
    }

    public void onConnected(@NotNull ConnectInPacket connect) {
        connection.send(new ConnectAckOutPacket(ConnectReasonCode.SUCCESSFUL, true));
    }
}
