package com.ss.mqtt.broker.network.client;

import com.ss.mqtt.broker.config.MqttConnectionConfig;
import com.ss.mqtt.broker.model.reason.code.ConnectAckReasonCode;
import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.handler.client.MqttClientReleaseHandler;
import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.mqtt.broker.factory.packet.out.MqttPacketOutFactory;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import com.ss.rlib.common.util.StringUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Log4j2
@ToString(of = "clientId")
public abstract class AbstractMqttClient implements UnsafeMqttClient {

    protected final @NotNull MqttConnection connection;
    protected final MqttClientReleaseHandler releaseHandler;
    protected final AtomicBoolean released;

    private volatile @Setter @NotNull String clientId;
    private volatile @Setter @Getter @Nullable MqttSession session;

    private volatile long sessionExpiryInterval;
    private volatile int receiveMax;
    private volatile int maximumPacketSize;
    private volatile int topicAliasMaximum;
    private volatile int keepAlive;

    private volatile boolean requestResponseInformation = false;
    private volatile boolean requestProblemInformation = false;

    public AbstractMqttClient(@NotNull MqttConnection connection, @NotNull MqttClientReleaseHandler releaseHandler) {
        this.connection = connection;
        this.releaseHandler = releaseHandler;
        this.released = new AtomicBoolean(false);
        this.clientId = StringUtils.EMPTY;
        var config = connection.getConfig();
        this.sessionExpiryInterval = config.getDefaultSessionExpiryInterval();
        this.receiveMax = config.getReceiveMaximum();
        this.maximumPacketSize = config.getMaximumPacketSize();
        this.topicAliasMaximum = config.getTopicAliasMaximum();
        this.keepAlive = config.getMinKeepAliveTime();
    }

    @Override
    public void handle(@NotNull MqttReadablePacket packet) {
        log.info("Handle received packet: {} : {}", packet.getName(), packet);

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
        int keepAlive,
        boolean requestResponseInformation,
        boolean requestProblemInformation
    ) {
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.receiveMax = receiveMax;
        this.maximumPacketSize = maximumPacketSize;
        this.topicAliasMaximum = topicAliasMaximum;
        this.keepAlive = keepAlive;
        this.requestProblemInformation = requestProblemInformation;
        this.requestResponseInformation = requestResponseInformation;
    }

    @Override
    public void send(@NotNull MqttWritablePacket packet) {
        connection.send(packet);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> sendWithFeedback(@NotNull MqttWritablePacket packet) {
        return connection.sendWithFeedback(packet);
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

    @Override
    public @NotNull Mono<?> release() {
        if (released.compareAndSet(false, true)) {
            return releaseHandler.release(this);
        } else {
            return Mono.empty();
        }
    }
}
