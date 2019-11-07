package com.ss.mqtt.broker.network.client.impl;

import com.ss.mqtt.broker.config.MqttConnectionConfig;
import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.client.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.factory.MqttPacketOutFactory;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import com.ss.rlib.common.util.StringUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Log4j2
@EqualsAndHashCode(of = "clientId")
public abstract class AbstractMqttClient implements UnsafeMqttClient {

    protected final @NotNull MqttConnection connection;

    private volatile @Setter @NotNull String clientId;
    private volatile @Setter @Getter @Nullable MqttSession session;

    private volatile long sessionExpiryInterval = MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_DEFAULT;
    private volatile int receiveMax = MqttPropertyConstants.RECEIVE_MAXIMUM_DEFAULT;
    private volatile int maximumPacketSize = MqttPropertyConstants.MAXIMUM_PACKET_SIZE_DEFAULT;
    private volatile int topicAliasMaximum = MqttPropertyConstants.TOPIC_ALIAS_MAXIMUM_DEFAULT;
    private volatile int keepAlive = MqttPropertyConstants.SERVER_KEEP_ALIVE_MAX;

    public AbstractMqttClient(@NotNull MqttConnection connection) {
        this.connection = connection;
        this.clientId = StringUtils.EMPTY;
    }

    @Override
    public void handle(@NotNull MqttReadablePacket packet) {
        log.info("Handle received packet: {}", packet);

        var packetHandler = connection.getPacketHandlers()[packet.getPacketType()];

        if (packetHandler != null) {
            packetHandler.handle(this, packet);
        } else {
            log.warn("No packet handler in client {} for packet {}", this, packet);
        }
    }

    @Override
    public void configure(
        long sessionExpiryInterval,
        int receiveMax,
        int maximumPacketSize,
        int topicAliasMaximum,
        int keepAlive
    ) {
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.receiveMax = receiveMax;
        this.maximumPacketSize = maximumPacketSize;
        this.topicAliasMaximum = topicAliasMaximum;
        this.keepAlive = keepAlive;
    }

    @Override
    public void send(@NotNull MqttWritablePacket packet) {
        connection.send(packet);
    }

    public void reject(@NotNull ConnectAckReasonCode reasonCode) {
        connection
            .sendWithFeedback(getPacketOutFactory().newConnectAck(this, reasonCode))
            .thenAccept(sent -> connection.close());
    }

    @Override
    public @NotNull MqttPacketOutFactory getPacketOutFactory() {
        return connection.getMqttVersion().getPacketOutFactory();
    }

    @Override
    public @NotNull MqttConnectionConfig getConnectionConfig() {
        return connection.getConfig();
    }
}
