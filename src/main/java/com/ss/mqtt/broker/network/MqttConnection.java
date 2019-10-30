package com.ss.mqtt.broker.network;

import com.ss.mqtt.broker.config.MqttConnectionConfig;
import com.ss.mqtt.broker.model.MqttVersion;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.network.packet.MqttPacketReader;
import com.ss.mqtt.broker.network.packet.MqttPacketWriter;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import com.ss.mqtt.broker.service.PublishingService;
import com.ss.mqtt.broker.service.SubscriptionService;
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

import java.nio.channels.AsynchronousSocketChannel;

@Log4j2
@Getter(AccessLevel.PROTECTED)
public class MqttConnection extends AbstractConnection<MqttReadablePacket, MqttWritablePacket> {

    private final PacketReader packetReader;
    private final PacketWriter packetWriter;

    private final SubscriptionService subscriptionService;
    private final PublishingService publishingService;

    private final @Getter @NotNull MqttClient client;
    private final @Getter @NotNull MqttConnectionConfig config;

    private volatile @Setter @NotNull MqttVersion mqttVersion;

    public MqttConnection(
        @NotNull Network<? extends Connection<MqttReadablePacket, MqttWritablePacket>> network,
        @NotNull AsynchronousSocketChannel channel,
        @NotNull NetworkCryptor crypt,
        @NotNull BufferAllocator bufferAllocator,
        int maxPacketsByRead,
        @NotNull SubscriptionService subscriptionService,
        @NotNull PublishingService publishingService
        @NotNull MqttConnectionConfig config
    ) {
        super(network, channel, crypt, bufferAllocator, maxPacketsByRead);
        this.config = config;
        this.mqttVersion = MqttVersion.MQTT_3_1_1;
        this.packetReader = createPacketReader();
        this.packetWriter = createPacketWriter();
        this.client = new MqttClient(this);
        this.subscriptionService = subscriptionService;
        this.publishingService = publishingService;
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
            this::handleReadPacket,
            maxPacketsByRead
        );
    }

    private @NotNull PacketWriter createPacketWriter() {
        return new MqttPacketWriter(
            this,
            channel,
            bufferAllocator,
            this::updateLastActivity,
            this::nextPacketToWrite
        );
    }

    @Override
    public @NotNull String toString() {
        return getRemoteAddress();
    }
}
