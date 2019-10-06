package com.ss.mqtt.broker.network.packet;

import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import com.ss.mqtt.broker.util.MqttDataUtils;
import com.ss.rlib.network.BufferAllocator;
import com.ss.rlib.network.packet.impl.AbstractPacketWriter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.function.Supplier;

public class MqttPacketWriter extends AbstractPacketWriter<MqttWritablePacket, MqttConnection> {

    public MqttPacketWriter(
        @NotNull MqttConnection connection,
        @NotNull AsynchronousSocketChannel channel,
        @NotNull BufferAllocator bufferAllocator,
        @NotNull Runnable updateActivityFunction,
        @NotNull Supplier<MqttWritablePacket> nextWritePacketSupplier
    ) {
        super(connection, channel, bufferAllocator, updateActivityFunction, nextWritePacketSupplier);
    }

    @Override
    protected int getTotalSize(@NotNull MqttWritablePacket packet, int expectedLength) {
        return 1 + MqttDataUtils.sizeOfMbi(expectedLength) + expectedLength;
    }

    @Override
    protected boolean onBeforeWrite(
        @NotNull MqttWritablePacket packet,
        int expectedLength,
        int totalSize,
        @NotNull ByteBuffer buffer
    ) {
        buffer.put((byte) packet.getPacketTypeAndFlags());
        MqttDataUtils.writeMbi(expectedLength, buffer);
        return true;
    }
}
