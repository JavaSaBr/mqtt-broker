package javasabr.mqtt.network.message;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
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
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.util.MqttDataUtils;
import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.common.util.NumberUtils;
import javasabr.rlib.functions.ByteFunction;
import lombok.CustomLog;
import org.jspecify.annotations.Nullable;

@CustomLog
public final class MqttPacketCodec {

  private static final int MAX_MBI_SIZE = 4;
  private static final int HEADER_TYPE_SIZE = 1;
  private static final int PAYLOAD_OFFSET = MAX_MBI_SIZE + HEADER_TYPE_SIZE;

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

  public int decodePacketLength(ByteBuffer buffer) {
    int prevPos = buffer.position();
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
  public MqttInMessage decodePacket(ByteBuffer buffer, int startPacketPosition) {
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

  public int calculateEncodedPacketSize(int payloadLength) {
    return PAYLOAD_OFFSET + payloadLength;
  }

  public void prepareEncodingBuffer(ByteBuffer buffer) {
    buffer
        .clear()
        .position(PAYLOAD_OFFSET);
  }

  public void encodeHeader(MqttOutMessage packet, ByteBuffer buffer) {
    int maxBufferPosition = buffer.position();
    int payloadSize = maxBufferPosition - PAYLOAD_OFFSET;
    int messageTypeAndFlagsOffset = MAX_MBI_SIZE - MqttDataUtils.sizeOfMbi(payloadSize);
    buffer
        .position(messageTypeAndFlagsOffset)
        .put((byte) packet.messageTypeAndFlags());
    MqttDataUtils
        .writeMbi(payloadSize, buffer)
        .position(messageTypeAndFlagsOffset)
        .limit(maxBufferPosition);
  }
}
