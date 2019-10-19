package com.ss.mqtt.broker.network;

import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.MqttVersion;
import com.ss.mqtt.broker.network.packet.factory.MqttPacketOutFactory;
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

@Log4j2
@Getter
public class MqttClient {

    private final @NotNull MqttConnection connection;

    private volatile @NotNull String clientId;
    private volatile @NotNull String serverClientId;

    private volatile long sessionExpiryInterval = MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_DEFAULT;
    private volatile int receiveMax = MqttPropertyConstants.RECEIVE_MAXIMUM_DEFAULT;
    private volatile int maximumPacketSize = MqttPropertyConstants.MAXIMUM_PACKET_SIZE_DEFAULT;
    private volatile int topicAliasMaximum = MqttPropertyConstants.TOPIC_ALIAS_MAXIMUM_DEFAULT;

    private volatile MqttVersion mqttVersion;

    public MqttClient(@NotNull MqttConnection connection) {
        this.connection = connection;
        this.mqttVersion = MqttVersion.MQTT_5;
        this.clientId = "";
        this.serverClientId = "";
    }

    public void handle(@NotNull MqttReadablePacket packet) {
        log.info("Handle received packet: {}", packet);
        switch (packet.getPacketType()) {
            case ConnectInPacket.PACKET_TYPE:
                onConnected((ConnectInPacket) packet);
                break;
        }
    }

    public void reject(@NotNull ConnectAckReasonCode returnCode) {
        connection.send(getPacketOutFactory().newConnectAck(this, returnCode, false));
    }

    public void onConnected(@NotNull ConnectInPacket connect) {
        mqttVersion = connect.getMqttVersion();
        sessionExpiryInterval = connect.getSessionExpiryInterval();
        receiveMax = connect.getReceiveMax();
        maximumPacketSize = connect.getMaximumPacketSize();
        clientId = connect.getClientId();
        serverClientId = connect.getClientId();
        topicAliasMaximum = connect.getTopicAliasMaximum();
        connection.send(getPacketOutFactory().newConnectAck(this, ConnectAckReasonCode.SUCCESSFUL, false));
    }

    private @NotNull MqttPacketOutFactory getPacketOutFactory() {
        return mqttVersion.getPacketOutFactory();
    }
}
