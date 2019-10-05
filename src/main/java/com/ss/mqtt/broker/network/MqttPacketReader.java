package com.ss.mqtt.broker.network;

import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import com.ss.mqtt.broker.util.MqttDataUtils;
import com.ss.rlib.network.BufferAllocator;
import com.ss.rlib.network.packet.impl.AbstractPacketReader;
import com.ss.rlib.network.packet.registry.ReadablePacketRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.function.Consumer;

public class MqttPacketReader extends AbstractPacketReader<MqttReadablePacket, MqttConnection> {

    public static final int PACKET_LENGTH_START_BYTE = 2;

    private final ReadablePacketRegistry<MqttReadablePacket> packetRegistry;

    public MqttPacketReader(
        @NotNull MqttConnection connection,
        @NotNull AsynchronousSocketChannel channel,
        @NotNull BufferAllocator bufferAllocator,
        @NotNull Runnable updateActivityFunction,
        @NotNull Consumer<MqttReadablePacket> readPacketHandler,
        int maxPacketsByRead,
        @NotNull ReadablePacketRegistry<MqttReadablePacket> packetRegistry
    ) {
        super(
            connection,
            channel,
            bufferAllocator,
            updateActivityFunction,
            readPacketHandler,
            maxPacketsByRead
        );
        this.packetRegistry = packetRegistry;
    }

    @Override
    protected boolean canStartReadPacket(@NotNull ByteBuffer buffer) {
        return buffer.remaining() >= PACKET_LENGTH_START_BYTE;
    }

    @Override
    protected int calcDataLength(int packetLength, int readBytes, @NotNull ByteBuffer buffer) {
        return packetLength - readBytes;
    }

    @Override
    protected int readPacketLength(@NotNull ByteBuffer buffer) {

        var prevPos = buffer.position();

        // skip first byte of packet type
        buffer.get();

        var dataSize = MqttDataUtils.readMbi(buffer);
        if (dataSize == -1) {
            return -1;
        }

        var readBytes = buffer.position() - prevPos;

        return ((int) dataSize) + readBytes;
    }

    @Override
    protected @Nullable MqttReadablePacket createPacketFor(
        @NotNull ByteBuffer buffer,
        int packetLength,
        int dataLength
    ) {
        return null;
    }
}
