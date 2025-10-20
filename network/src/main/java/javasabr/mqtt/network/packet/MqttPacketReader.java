package javasabr.mqtt.network.packet;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.packet.in.AuthenticationInPacket;
import javasabr.mqtt.network.packet.in.ConnectAckInPacket;
import javasabr.mqtt.network.packet.in.ConnectInPacket;
import javasabr.mqtt.network.packet.in.DisconnectInPacket;
import javasabr.mqtt.network.packet.in.MqttReadablePacket;
import javasabr.mqtt.network.packet.in.PingRequestInPacket;
import javasabr.mqtt.network.packet.in.PingResponseInPacket;
import javasabr.mqtt.network.packet.in.PublishAckInPacket;
import javasabr.mqtt.network.packet.in.PublishCompleteInPacket;
import javasabr.mqtt.network.packet.in.PublishInPacket;
import javasabr.mqtt.network.packet.in.PublishReceivedInPacket;
import javasabr.mqtt.network.packet.in.PublishReleaseInPacket;
import javasabr.mqtt.network.packet.in.SubscribeAckInPacket;
import javasabr.mqtt.network.packet.in.SubscribeInPacket;
import javasabr.mqtt.network.packet.in.UnsubscribeAckInPacket;
import javasabr.mqtt.network.packet.in.UnsubscribeInPacket;
import javasabr.mqtt.network.utils.MqttDataUtils;
import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.common.util.NumberUtils;
import javasabr.rlib.functions.ByteFunction;
import javasabr.rlib.network.packet.impl.AbstractNetworkPacketReader;
import org.jspecify.annotations.Nullable;

public class MqttPacketReader extends AbstractNetworkPacketReader<MqttReadablePacket, MqttConnection> {

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
      Runnable updateActivityFunction,
      Consumer<MqttReadablePacket> readPacketHandler,
      int maxPacketsByRead) {
    super(connection, updateActivityFunction, readPacketHandler, maxPacketsByRead);
  }

  @Override
  protected boolean canStartReadPacket(ByteBuffer buffer) {
    return buffer.remaining() >= PACKET_LENGTH_START_BYTE;
  }

  @Override
  protected int readFullPacketLength(ByteBuffer buffer) {
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901021
    int prevPos = buffer.position();

    // skip first byte of packet type
    buffer.get();
    int dataSize = MqttDataUtils.readMbi(buffer);
    if (dataSize == -1) {
      return -1;
    }

    int readBytes = buffer.position() - prevPos;
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
