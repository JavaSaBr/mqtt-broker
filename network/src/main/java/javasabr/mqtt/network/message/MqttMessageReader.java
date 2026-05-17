package javasabr.mqtt.network.message;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.in.AuthenticationMqttInMessage;
import javasabr.mqtt.network.message.in.ConnectAckMqttInMessage;
import javasabr.mqtt.network.message.in.ConnectMqttInMessage;
import javasabr.mqtt.network.message.in.DisconnectMqttInMessage;
import javasabr.mqtt.network.message.in.MqttInMessage;
import javasabr.mqtt.network.message.in.PingRequestMqttInMessage;
import javasabr.mqtt.network.message.in.PingResponseMqttInMessage;
import javasabr.mqtt.network.message.in.PublishAckMqttInMessage;
import javasabr.mqtt.network.message.in.PublishCompleteMqttInMessage;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import javasabr.mqtt.network.message.in.PublishReceivedMqttInMessage;
import javasabr.mqtt.network.message.in.PublishReleaseMqttInMessage;
import javasabr.mqtt.network.message.in.SubscribeAckMqttInMessage;
import javasabr.mqtt.network.message.in.SubscribeMqttInMessage;
import javasabr.mqtt.network.message.in.UnsubscribeAckMqttInMessage;
import javasabr.mqtt.network.message.in.UnsubscribeMqttInMessage;
import javasabr.mqtt.network.util.MqttDataUtils;
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
      _ -> {
        throw new NoSuchElementException("Unknown MQTT message with id:[0]");
      },
      ConnectMqttInMessage::new,
      ConnectAckMqttInMessage::new,
      PublishMqttInMessage::new,
      PublishAckMqttInMessage::new,
      PublishReceivedMqttInMessage::new,
      PublishReleaseMqttInMessage::new,
      PublishCompleteMqttInMessage::new,
      SubscribeMqttInMessage::new,
      SubscribeAckMqttInMessage::new,
      UnsubscribeMqttInMessage::new,
      UnsubscribeAckMqttInMessage::new,
      PingRequestMqttInMessage::new,
      PingResponseMqttInMessage::new,
      DisconnectMqttInMessage::new,
      AuthenticationMqttInMessage::new);

  public MqttMessageReader(
      MqttConnection connection,
      Runnable updateActivityFunction,
      Consumer<MqttInMessage> validPacketHandler,
      Consumer<MqttInMessage> invalidPacketHandler,
      int maxPacketsByRead) {
    super(connection, updateActivityFunction, validPacketHandler, invalidPacketHandler, maxPacketsByRead);
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
      buffer.position(prevPos);
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
