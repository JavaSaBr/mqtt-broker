package com.ss.mqtt.broker.network.packet;

import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.in.AuthenticationInPacket;
import com.ss.mqtt.broker.network.packet.in.ConnectAckInPacket;
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket;
import com.ss.mqtt.broker.network.packet.in.DisconnectInPacket;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import com.ss.mqtt.broker.network.packet.in.PingRequestInPacket;
import com.ss.mqtt.broker.network.packet.in.PingResponseInPacket;
import com.ss.mqtt.broker.network.packet.in.PublishAckInPacket;
import com.ss.mqtt.broker.network.packet.in.PublishCompleteInPacket;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.mqtt.broker.network.packet.in.PublishReceivedInPacket;
import com.ss.mqtt.broker.network.packet.in.PublishReleaseInPacket;
import com.ss.mqtt.broker.network.packet.in.SubscribeAckInPacket;
import com.ss.mqtt.broker.network.packet.in.SubscribeInPacket;
import com.ss.mqtt.broker.network.packet.in.UnsubscribeAckInPacket;
import com.ss.mqtt.broker.network.packet.in.UnsubscribeInPacket;
import com.ss.mqtt.broker.util.MqttDataUtils;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.common.util.NumberUtils;
import javasabr.rlib.functions.ByteFunction;
import javasabr.rlib.network.BufferAllocator;
import javasabr.rlib.network.packet.impl.AbstractPacketReader;
import org.jspecify.annotations.Nullable;

public class MqttPacketReader extends AbstractPacketReader<MqttReadablePacket, MqttConnection> {

  private static final int PACKET_LENGTH_START_BYTE = 2;

  private static final ByteFunction<MqttReadablePacket>[] PACKET_FACTORIES = ArrayUtils.array(
      id -> {throw new NoSuchElementException();},
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
      AuthenticationInPacket::new);

  public MqttPacketReader(
      MqttConnection connection,
      AsynchronousSocketChannel channel,
      BufferAllocator bufferAllocator,
      Runnable updateActivityFunction,
      Consumer<MqttReadablePacket> readPacketHandler,
      int maxPacketsByRead) {
    super(connection, channel, bufferAllocator, updateActivityFunction, readPacketHandler, maxPacketsByRead);
  }

  @Override
  protected boolean canStartReadPacket(ByteBuffer buffer) {
    return buffer.remaining() >= PACKET_LENGTH_START_BYTE;
  }

  @Override
  protected int getDataLength(int packetLength, int readBytes, ByteBuffer buffer) {
    return packetLength - readBytes;
  }

  @Override
  protected int readPacketLength(ByteBuffer buffer) {

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

  @Nullable
  @Override
  protected MqttReadablePacket createPacketFor(
      ByteBuffer buffer,
      int startPacketPosition,
      int packetLength,
      int dataLength) {

    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901021
    var startByte = Byte.toUnsignedInt(buffer.get(startPacketPosition));
    var type = NumberUtils.getHighByteBits(startByte);
    var info = NumberUtils.getLowByteBits(startByte);

    return PACKET_FACTORIES[type].apply(info);
  }
}
