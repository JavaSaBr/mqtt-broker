package com.ss.mqtt.broker.network;

import static com.ss.mqtt.broker.network.packet.PacketType.CONNECT_REQUEST;
import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.MqttVersion;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.mqtt.broker.network.packet.factory.MqttPacketOutFactory;
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import com.ss.mqtt.broker.network.packet.in.SubscribeInPacket;
import com.ss.mqtt.broker.service.SubscriptionService;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Log4j2
@Getter
public class MqttClient {

    private final SubscriptionService subscriptionService;

    private final @NotNull MqttConnection connection;

    private volatile @NotNull String clientId;
    private volatile @NotNull String serverClientId;

    private volatile long sessionExpiryInterval = MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_DEFAULT;
    private volatile int receiveMax = MqttPropertyConstants.RECEIVE_MAXIMUM_DEFAULT;
    private volatile int maximumPacketSize = MqttPropertyConstants.MAXIMUM_PACKET_SIZE_DEFAULT;
    private volatile int topicAliasMaximum = MqttPropertyConstants.TOPIC_ALIAS_MAXIMUM_DEFAULT;

    private volatile MqttVersion mqttVersion;


    public MqttClient(@NotNull MqttConnection connection, @NotNull SubscriptionService subscriptionService) {
        this.connection = connection;
        this.mqttVersion = MqttVersion.MQTT_5;
        this.subscriptionService = subscriptionService;
        this.clientId = "";
        this.serverClientId = "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MqttClient that = (MqttClient) o;
        return clientId.equals(that.clientId) && serverClientId.equals(that.serverClientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, serverClientId);
    }

    public void handle(@NotNull MqttReadablePacket packet) {
        log.info("Handle received packet: {}", packet);
        switch (PacketType.fromByte(packet.getPacketType())) {
            case CONNECT_REQUEST:
                onConnected((ConnectInPacket) packet);
                break;
            case SUBSCRIBE:
                onSubscribe((SubscribeInPacket) packet);
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

    public void onSubscribe(@NotNull SubscribeInPacket subscribe) {

        subscriptionService.subscribe(connection.getClient(), subscribe.getTopicFilters());
        connection.send(getPacketOutFactory().newConnectAck(this, ConnectAckReasonCode.SUCCESSFUL, false));
    }

    private @NotNull MqttPacketOutFactory getPacketOutFactory() {
        return mqttVersion.getPacketOutFactory();
    }
}
