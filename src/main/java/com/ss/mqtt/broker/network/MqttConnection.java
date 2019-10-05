package com.ss.mqtt.broker.network;

import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import com.ss.mqtt.broker.network.packet.MqttWritablePacket;
import com.ss.rlib.network.BufferAllocator;
import com.ss.rlib.network.Connection;
import com.ss.rlib.network.Network;
import com.ss.rlib.network.NetworkCryptor;
import com.ss.rlib.network.impl.AbstractConnection;
import com.ss.rlib.network.packet.PacketReader;
import com.ss.rlib.network.packet.PacketWriter;
import com.ss.rlib.network.packet.impl.IdBasedPacketWriter;
import com.ss.rlib.network.packet.registry.ReadablePacketRegistry;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.channels.AsynchronousSocketChannel;

@Getter(AccessLevel.PROTECTED)
public class MqttConnection extends AbstractConnection<MqttReadablePacket, MqttWritablePacket> {

    private final ReadablePacketRegistry<MqttReadablePacket> packetRegistry;
    private final PacketReader packetReader;
    private final PacketWriter packetWriter;

    public MqttConnection(
        @NotNull Network<? extends Connection<MqttReadablePacket, MqttWritablePacket>> network,
        @NotNull AsynchronousSocketChannel channel,
        @NotNull NetworkCryptor crypt,
        @NotNull BufferAllocator bufferAllocator,
        @NotNull ReadablePacketRegistry<MqttReadablePacket> packetRegistry,
        int maxPacketsByRead
    ) {
        super(
            network,
            channel,
            crypt,
            bufferAllocator,
            maxPacketsByRead
        );
        this.packetRegistry = packetRegistry;
        this.packetReader = createPacketReader();
        this.packetWriter = createPacketWriter();
    }

    protected @NotNull PacketReader createPacketReader() {
        return new MqttPacketReader(
            this,
            channel,
            bufferAllocator,
            this::updateLastActivity,
            this::handleReadPacket,
            maxPacketsByRead,
            getPacketRegistry()
        );
    }

    protected @NotNull PacketWriter createPacketWriter() {
        return new IdBasedPacketWriter<>(
            this,
            channel,
            bufferAllocator,
            this::updateLastActivity,
            this::nextPacketToWrite,
            0,
            0
        );
    }
}
