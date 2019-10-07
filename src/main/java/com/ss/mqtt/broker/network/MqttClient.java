package com.ss.mqtt.broker.network;

import com.ss.mqtt.broker.model.ConnectReasonCode;
import com.ss.mqtt.broker.model.MqttVersion;
import com.ss.mqtt.broker.network.packet.factory.MqttPacketOutFactory;
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class MqttClient {

    private final @Getter MqttConnection connection;

    private volatile MqttVersion mqttVersion;

    public MqttClient(@NotNull MqttConnection connection) {
        this.connection = connection;
        this.mqttVersion = MqttVersion.MQTT_5;
    }

    public void reject(@NotNull ConnectReasonCode returnCode) {
        connection.send(getPacketOutFactory().newConnectAck(this, returnCode, false));
    }

    public void onConnected(@NotNull ConnectInPacket connect) {
        this.mqttVersion = connect.getMqttVersion();
        connection.send(getPacketOutFactory().newConnectAck(this, ConnectReasonCode.SUCCESSFUL, false));
    }

    private @NotNull MqttPacketOutFactory getPacketOutFactory() {
        return mqttVersion.getPacketOutFactory();
    }
}
