package com.ss.mqtt.broker.network.packet;

import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.in.*;
import com.ss.mqtt.broker.util.MqttDataUtils;
import com.ss.rlib.common.function.ByteFunction;
import com.ss.rlib.common.function.NotNullConsumer;
import com.ss.rlib.common.util.NumberUtils;
import com.ss.rlib.common.util.array.ArrayFactory;
import com.ss.rlib.network.BufferAllocator;
import com.ss.rlib.network.packet.impl.AbstractPacketReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

public class MqttPacketReader extends AbstractPacketReader<MqttReadablePacket, MqttConnection> {

    private static final int PACKET_LENGTH_START_BYTE = 2;

    private static final ByteFunction<MqttReadablePacket>[] PACKET_FACTORIES = ArrayFactory.toArray(
        null,
        ConnectInPacket::new,
        ConnectAckInPacket::new,
        PublishInPacket::new,
        PublishAckInPacket::new,
        PublishReceivedInPacket::new,
        PublishReleaseInPacket::new,
        PublishCompleteInPacket::new,
        SubscribeInPacket::new,
        SubscribeAckInPacket::new,
        UnsubscribeInPacket::new,
        UnsubscribeAckInPacket::new,
        PingRequestInPacket::new,
        PingResponseInPacket::new,
        DisconnectInPacket::new,
        AuthenticationInPacket::new
    );

    @Override
    protected boolean canStartReadPacket(@NotNull ByteBuffer buffer) {
        return buffer.remaining() >= PACKET_LENGTH_START_BYTE;
    }

    public MqttPacketReader(
        @NotNull MqttConnection connection,
        @NotNull AsynchronousSocketChannel channel,
        @NotNull BufferAllocator bufferAllocator,
        @NotNull Runnable updateActivityFunction,
        @NotNull NotNullConsumer<MqttReadablePacket> readPacketHandler,
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
    protected int getDataLength(int packetLength, int readBytes, @NotNull ByteBuffer buffer) {
        return packetLength - readBytes;
    }

    @Override
    protected int readPacketLength(@NotNull ByteBuffer buffer) {

        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901021
        var prevPos = buffer.position();

        // skip first byte of packet type
        buffer.get();

        var dataSize = MqttDataUtils.readMbi(buffer);
        if (dataSize == -1) {
            return -1;
        }

        var readBytes = buffer.position() - prevPos;

        return dataSize + readBytes;
    }

    @Override
    protected @Nullable MqttReadablePacket createPacketFor(
        @NotNull ByteBuffer buffer,
        int startPacketPosition,
        int packetLength,
        int dataLength
    ) {

        // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901021
        var startByte = Byte.toUnsignedInt(buffer.get(startPacketPosition));
        var type = NumberUtils.getHighByteBits(startByte);
        var info = NumberUtils.getLowByteBits(startByte);

        return PACKET_FACTORIES[type].apply(info);
    }

    @Override
    protected void readAndHandlePacket(
        @NotNull ByteBuffer bufferToRead,
        int dataLength,
        @NotNull MqttReadablePacket packet
    ) {
        packet.read(this.connection, bufferToRead, dataLength);
        readPacketHandler.accept(packet);
    }
}
