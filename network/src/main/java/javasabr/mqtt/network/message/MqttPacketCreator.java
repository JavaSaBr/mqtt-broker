package javasabr.mqtt.network.message;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import javasabr.mqtt.network.message.in.*;
import javasabr.mqtt.network.util.MqttDataUtils;
import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.common.util.NumberUtils;
import javasabr.rlib.functions.ByteFunction;
import lombok.CustomLog;
import org.jspecify.annotations.Nullable;

@CustomLog
public final class MqttPacketCreator {

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

  public int readFullPacketLength(ByteBuffer buffer) {
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
  public MqttInMessage createPacketFor(ByteBuffer buffer, int startPacketPosition) {
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
