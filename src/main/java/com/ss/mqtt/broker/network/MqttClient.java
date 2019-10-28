package com.ss.mqtt.broker.network;

import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.SubscribeAckReasonCode;
import com.ss.mqtt.broker.model.UnsubscribeAckReasonCode;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.mqtt.broker.network.packet.factory.MqttPacketOutFactory;
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import com.ss.mqtt.broker.network.packet.in.SubscribeInPacket;
import com.ss.mqtt.broker.network.packet.in.UnsubscribeInPacket;
import com.ss.rlib.common.util.array.Array;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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

    public MqttClient(@NotNull MqttConnection connection) {
        this.connection = connection;
        this.clientId = "";
        this.serverClientId = "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (MqttClient) o;
        return serverClientId.equals(that.serverClientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverClientId);
    }

    public void handle(@NotNull MqttReadablePacket packet) {
        log.info("Handle received packet: {}", packet);
        switch (PacketType.fromByte(packet.getPacketType())) {
            case CONNECT:
                onConnected((ConnectInPacket) packet);
                break;
            case SUBSCRIBE:
                onSubscribe((SubscribeInPacket) packet);
                break;
            case UNSUBSCRIBE:
                onUnsubscribe((UnsubscribeInPacket) packet);
                break;
        }
    }

    public void reject(@NotNull ConnectAckReasonCode returnCode) {
        connection.send(getPacketOutFactory().newConnectAck(this, returnCode, false));
    }

    public void onConnected(@NotNull ConnectInPacket connect) {
        connection.setMqttVersion(connect.getMqttVersion());
        sessionExpiryInterval = connect.getSessionExpiryInterval();
        receiveMax = connect.getReceiveMax();
        maximumPacketSize = connect.getMaximumPacketSize();
        clientId = connect.getClientId();
        serverClientId = connect.getClientId();
        topicAliasMaximum = connect.getTopicAliasMaximum();
        connection.send(getPacketOutFactory().newConnectAck(this, ConnectAckReasonCode.SUCCESSFUL, false));
    }

    public void onSubscribe(@NotNull SubscribeInPacket subscribe) {
        var ackReasonCodes = connection.getSubscriptionService()
            .subscribe(connection.getClient(), subscribe.getTopicFilters());
        connection.send(getPacketOutFactory().newSubscribeAck(connection.getClient(),
            subscribe.getPacketId(),
            ackReasonCodes
        ));
    }

    public void onUnsubscribe(@NotNull UnsubscribeInPacket subscribe) {
        var ackReasonCodes = connection.getSubscriptionService()
            .unsubscribe(connection.getClient(), subscribe.getTopicFilters());
        connection.send(getPacketOutFactory().newUnsubscribeAck(connection.getClient(),
            subscribe.getPacketId(),
            ackReasonCodes
        ));
    }

    private @NotNull MqttPacketOutFactory getPacketOutFactory() {
        return connection.getMqttVersion().getPacketOutFactory();
    }
}
