package com.ss.mqtt.broker.network.packet;

import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.in.ConnectRequestInPacket;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import com.ss.mqtt.broker.util.MqttDataUtils;
import com.ss.rlib.common.function.ByteFunction;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.network.BufferAllocator;
import com.ss.rlib.network.packet.impl.AbstractPacketReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.function.Consumer;

public class MqttPacketReader extends AbstractPacketReader<MqttReadablePacket, MqttConnection> {

    public static final int PACKET_LENGTH_START_BYTE = 2;

    private static final ByteFunction<MqttReadablePacket>[] PACKET_FACTORIES = ArrayFactory.toArray(
        null,
        ConnectRequestInPacket::new
    );

    public MqttPacketReader(
        @NotNull MqttConnection connection,
        @NotNull AsynchronousSocketChannel channel,
        @NotNull BufferAllocator bufferAllocator,
        @NotNull Runnable updateActivityFunction,
        @NotNull Consumer<MqttReadablePacket> readPacketHandler,
        int maxPacketsByRead
    ) {
        super(
            connection,
            channel,
            bufferAllocator,
            updateActivityFunction,
            readPacketHandler,
            maxPacketsByRead
        );
    }

    @Override
    protected boolean canStartReadPacket(@NotNull ByteBuffer buffer) {
        return buffer.remaining() >= PACKET_LENGTH_START_BYTE;
    }

    @Override
    protected int getDataLength(int packetLength, int readBytes, @NotNull ByteBuffer buffer) {
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
        int startPacketPosition,
        int packetLength,
        int dataLength
    ) {

        var startByte = buffer.get(startPacketPosition);
        var type = (byte) (startByte >> 4);
        var info = (byte) (startByte & 0x0f);

        return PACKET_FACTORIES[type].apply(info);
    }
}
