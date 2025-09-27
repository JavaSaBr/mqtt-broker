package com.ss.mqtt.broker.network.packet;

import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import com.ss.mqtt.broker.util.MqttDataUtils;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javasabr.rlib.network.BufferAllocator;
import javasabr.rlib.network.packet.WritablePacket;
import javasabr.rlib.network.packet.impl.AbstractPacketWriter;
import org.jspecify.annotations.Nullable;

public class MqttPacketWriter extends AbstractPacketWriter<MqttWritablePacket, MqttConnection> {

    public MqttPacketWriter(
        MqttConnection connection,
        AsynchronousSocketChannel channel,
        BufferAllocator bufferAllocator,
        Runnable updateActivityFunction,
        Supplier<@Nullable WritablePacket> nextWritePacketSupplier,
        Consumer<WritablePacket> writtenPacketHandler,
        BiConsumer<WritablePacket, Boolean> sentPacketHandler
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
    protected int getTotalSize(WritablePacket packet, int expectedLength) {
        return 1 + MqttDataUtils.sizeOfMbi(expectedLength) + expectedLength;
    }

    @Override
    protected boolean onBeforeWrite(
        MqttWritablePacket packet,
        int expectedLength,
        int totalSize,
        ByteBuffer firstBuffer,
        ByteBuffer secondBuffer
    ) {
        firstBuffer.clear();
        secondBuffer.clear();
        return true;
    }

    @Override
    protected boolean onWrite(
        MqttWritablePacket packet,
        int expectedLength,
        int totalSize,
        ByteBuffer firstBuffer,
        ByteBuffer secondBuffer
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
        MqttWritablePacket packet,
        int expectedLength,
        int totalSize,
        ByteBuffer firstBuffer,
        ByteBuffer secondBuffer
    ) {
        firstBuffer.put((byte) packet.getPacketTypeAndFlags());
        MqttDataUtils.writeMbi(secondBuffer.remaining(), firstBuffer);
        firstBuffer.put(secondBuffer).flip();
        return true;
    }
}
