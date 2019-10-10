package com.ss.mqtt.broker.network;

import com.ss.mqtt.broker.model.ConnectReasonCode;
import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.MqttVersion;
import com.ss.mqtt.broker.network.packet.factory.MqttPacketOutFactory;
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class MqttClient {

    private final @Getter MqttConnection connection;

    private volatile @Getter String clientId;
    private volatile @Getter String serverClientId;

    private volatile @Getter long sessionExpiryInterval = MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_DEFAULT;
    private volatile @Getter int receiveMax = MqttPropertyConstants.RECEIVE_MAXIMUM_DEFAULT;
    private volatile @Getter int maximumPacketSize = MqttPropertyConstants.MAXIMUM_PACKET_SIZE_DEFAULT;
    private volatile @Getter int topicAliasMaximum = MqttPropertyConstants.TOPIC_ALIAS_MAXIMUM_DEFAULT;

    private volatile @Getter  MqttVersion mqttVersion;

    public MqttClient(@NotNull MqttConnection connection) {
        this.connection = connection;
        this.mqttVersion = MqttVersion.MQTT_5;
    }

    public void reject(@NotNull ConnectReasonCode returnCode) {
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

        connection.send(getPacketOutFactory().newConnectAck(this, ConnectReasonCode.SUCCESSFUL, false));
    }

    private @NotNull MqttPacketOutFactory getPacketOutFactory() {
        return mqttVersion.getPacketOutFactory();
    }
}
