package com.ss.mqtt.broker.network;

import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import com.ss.mqtt.broker.network.packet.MqttWritablePacket;
import com.ss.rlib.network.BufferAllocator;
import com.ss.rlib.network.Connection;
import com.ss.rlib.network.Network;
import com.ss.rlib.network.NetworkCryptor;
import com.ss.rlib.network.impl.IdBasedPacketConnection;
import com.ss.rlib.network.packet.registry.ReadablePacketRegistry;
import org.jetbrains.annotations.NotNull;

import java.nio.channels.AsynchronousSocketChannel;

public class MqttConnection extends IdBasedPacketConnection<MqttReadablePacket, MqttWritablePacket> {

    public MqttConnection(
        @NotNull Network<? extends Connection<MqttReadablePacket, MqttWritablePacket>> network,
        @NotNull AsynchronousSocketChannel channel,
        @NotNull NetworkCryptor crypt,
        @NotNull BufferAllocator bufferAllocator,
        @NotNull ReadablePacketRegistry<MqttReadablePacket> packetRegistry,
        int maxPacketsByRead,
        int packetLengthHeaderSize,
        int packetIdHeaderSize
    ) {
        super(
            network,
            channel,
            crypt,
            bufferAllocator,
            packetRegistry,
            maxPacketsByRead,
            packetLengthHeaderSize,
            packetIdHeaderSize
        );
    }
}
