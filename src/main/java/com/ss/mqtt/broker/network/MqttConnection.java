package com.ss.mqtt.broker.network;

import com.ss.mqtt.broker.config.MqttConnectionConfig;
import com.ss.mqtt.broker.model.MqttSession;
import com.ss.mqtt.broker.model.MqttVersion;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.MqttPacketReader;
import com.ss.mqtt.broker.network.packet.MqttPacketWriter;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import com.ss.mqtt.broker.handler.packet.in.PacketInHandler;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import com.ss.rlib.network.BufferAllocator;
import com.ss.rlib.network.Connection;
import com.ss.rlib.network.Network;
import com.ss.rlib.network.NetworkCryptor;
import com.ss.rlib.network.impl.AbstractConnection;
import com.ss.rlib.network.packet.PacketReader;
import com.ss.rlib.network.packet.PacketWriter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.function.Function;

@Log4j2
public class MqttConnection extends AbstractConnection<MqttReadablePacket, MqttWritablePacket> {

    @Getter(AccessLevel.PROTECTED)
    private final @NotNull PacketReader packetReader;

    @Getter(AccessLevel.PROTECTED)
    private final @NotNull PacketWriter packetWriter;

    private final @Getter PacketInHandler @NotNull [] packetHandlers;

    private final @Getter @NotNull UnsafeMqttClient client;
    private final @Getter @NotNull MqttConnectionConfig config;

    private volatile @Getter @Setter @NotNull MqttVersion mqttVersion;
    private volatile @Getter @Setter @Nullable MqttSession session;

    public MqttConnection(
        @NotNull Network<? extends Connection<MqttReadablePacket, MqttWritablePacket>> network,
        @NotNull AsynchronousSocketChannel channel,
        @NotNull BufferAllocator bufferAllocator,
        int maxPacketsByRead,
        PacketInHandler @NotNull [] packetHandlers,
        @NotNull MqttConnectionConfig config,
        @NotNull Function<MqttConnection, UnsafeMqttClient> clientFactory
    ) {
        super(network, channel, bufferAllocator, maxPacketsByRead);
        this.packetHandlers = packetHandlers;
        this.config = config;
        this.mqttVersion = MqttVersion.MQTT_3_1_1;
        this.packetReader = createPacketReader();
        this.packetWriter = createPacketWriter();
        this.client = clientFactory.apply(this);
    }

    public boolean isSupported(@NotNull MqttVersion mqttVersion) {
        return this.mqttVersion.ordinal() >= mqttVersion.ordinal();
    }

    private @NotNull PacketReader createPacketReader() {
        return new MqttPacketReader(
            this,
            channel,
            bufferAllocator,
            this::updateLastActivity,
            this::handleReceivedPacket,
            maxPacketsByRead
        );
    }

    private @NotNull PacketWriter createPacketWriter() {
        return new MqttPacketWriter(
            this,
            channel,
            bufferAllocator,
            this::updateLastActivity,
            this::nextPacketToWrite,
            this::onWrittenPacket,
            this::onSentPacket
        );
    }


    @Override
    public @NotNull String toString() {
        return getRemoteAddress();
    }

    @Override
    protected void doClose() {
        client.release().subscribe();
        super.doClose();
    }
}
