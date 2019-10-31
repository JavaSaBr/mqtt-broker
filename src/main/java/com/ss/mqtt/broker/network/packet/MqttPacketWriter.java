package com.ss.mqtt.broker.network.packet;

import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import com.ss.mqtt.broker.util.MqttDataUtils;
import com.ss.rlib.network.BufferAllocator;
import com.ss.rlib.network.packet.WritablePacket;
import com.ss.rlib.network.packet.impl.AbstractPacketWriter;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MqttPacketWriter extends AbstractPacketWriter<MqttWritablePacket, MqttConnection> {

    public MqttPacketWriter(
        @NotNull MqttConnection connection,
        @NotNull AsynchronousSocketChannel channel,
        @NotNull BufferAllocator bufferAllocator,
        @NotNull Runnable updateActivityFunction,
        @NotNull Supplier<@NotNull WritablePacket> nextWritePacketSupplier,
        @NotNull Consumer<@NotNull WritablePacket> writtenPacketHandler,
        @NotNull BiConsumer<@NotNull WritablePacket, Boolean> sentPacketHandler
    ) {
        super(
            connection,
            channel,
            bufferAllocator,
            updateActivityFunction,
            nextWritePacketSupplier,
            writtenPacketHandler,
            sentPacketHandler
        );
    }

    @Override
    protected int getTotalSize(@NotNull WritablePacket packet, int expectedLength) {
        return 1 + MqttDataUtils.sizeOfMbi(expectedLength) + expectedLength;
    }

    @Override
    protected boolean onBeforeWrite(
        @NotNull MqttWritablePacket packet,
        int expectedLength,
        int totalSize,
        @NotNull ByteBuffer firstBuffer,
        @NotNull ByteBuffer secondBuffer
    ) {
        firstBuffer.clear();
        secondBuffer.clear();
        return true;
    }

    @Override
    protected boolean onWrite(
        @NotNull MqttWritablePacket packet,
        int expectedLength,
        int totalSize,
        @NotNull ByteBuffer firstBuffer,
        @NotNull ByteBuffer secondBuffer
    ) {
        if (!packet.write(secondBuffer)) {
            return false;
        } else {
            secondBuffer.flip();
            return true;
        }
    }

    @Override
    protected boolean onAfterWrite(
        @NotNull MqttWritablePacket packet,
        int expectedLength,
        int totalSize,
        @NotNull ByteBuffer firstBuffer,
        @NotNull ByteBuffer secondBuffer
    ) {
        firstBuffer.put((byte) packet.getPacketTypeAndFlags());
        MqttDataUtils.writeMbi(secondBuffer.remaining(), firstBuffer);
        firstBuffer.put(secondBuffer).flip();
        return true;
    }
}
