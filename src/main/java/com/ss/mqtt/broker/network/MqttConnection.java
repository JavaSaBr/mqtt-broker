package com.ss.mqtt.broker.network;

import com.ss.mqtt.broker.network.packet.MqttPacketReader;
import com.ss.mqtt.broker.network.packet.MqttPacketWriter;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
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
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.nio.channels.AsynchronousSocketChannel;

@Log4j2
@Getter(AccessLevel.PROTECTED)
public class MqttConnection extends AbstractConnection<MqttReadablePacket, MqttWritablePacket> {

    private final PacketReader packetReader;
    private final PacketWriter packetWriter;

    private final @Getter MqttClient client;

    public MqttConnection(
        @NotNull Network<? extends Connection<MqttReadablePacket, MqttWritablePacket>> network,
        @NotNull AsynchronousSocketChannel channel,
        @NotNull NetworkCryptor crypt,
        @NotNull BufferAllocator bufferAllocator,
        int maxPacketsByRead
    ) {
        super(
            network,
            channel,
            crypt,
            bufferAllocator,
            maxPacketsByRead
        );
        this.packetReader = createPacketReader();
        this.packetWriter = createPacketWriter();
        this.client = new MqttClient(this);
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
