package javasabr.mqtt.network.message;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.in.AuthenticationMqttInMessage;
import javasabr.mqtt.network.message.in.ConnectAckMqttInMessage;
import javasabr.mqtt.network.message.in.ConnectMqttInMessage;
import javasabr.mqtt.network.packet.in.DisconnectInPacket;
import javasabr.mqtt.network.message.in.MqttInMessage;
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
import lombok.CustomLog;
import org.jspecify.annotations.Nullable;

@CustomLog
public class MqttMessageReader extends AbstractNetworkPacketReader<MqttInMessage, MqttConnection> {

  private static final int PACKET_LENGTH_START_BYTE = 2;

  private static final ByteFunction<MqttInMessage>[] PACKET_FACTORIES = ArrayUtils.array(
      id -> {
        throw new NoSuchElementException("Unknown MQTT message with id:["+ id + "]");
      },
      ConnectMqttInMessage::new,
      ConnectAckMqttInMessage::new,
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
      AuthenticationMqttInMessage::new);

  public MqttMessageReader(
      MqttConnection connection,
      Runnable updateActivityFunction,
      Consumer<MqttInMessage> readPacketHandler,
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
  protected MqttInMessage createPacketFor(
      ByteBuffer buffer,
      int startPacketPosition,
      int packetLength,
      int dataLength) {

    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901021
    int firstByte = Byte.toUnsignedInt(buffer.get(startPacketPosition));
    byte type = NumberUtils.getHighByteBits(firstByte);
    byte info = NumberUtils.getLowByteBits(firstByte);

    try {
      return PACKET_FACTORIES[type].apply(info);
    } catch (NoSuchElementException | NullPointerException e) {
      log.error(e.getMessage());
      return null;
    }
  }
}
