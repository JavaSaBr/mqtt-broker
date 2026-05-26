package javasabr.mqtt.network.message.plain;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttPacketCodec;
import javasabr.mqtt.network.message.in.MqttInMessage;
import javasabr.rlib.network.packet.impl.AbstractNetworkPacketReader;
import lombok.CustomLog;
import org.jspecify.annotations.Nullable;

@CustomLog
public class PlainMqttMessageReader extends AbstractNetworkPacketReader<MqttInMessage, MqttConnection> {

  private static final int PACKET_LENGTH_START_BYTE = 2;

  private final MqttPacketCodec mqttPacketCodec;

  public PlainMqttMessageReader(
      MqttConnection connection,
      Runnable updateActivityFunction,
      Consumer<MqttInMessage> validPacketHandler,
      Consumer<MqttInMessage> invalidPacketHandler,
      int maxPacketsByRead,
      MqttPacketCodec mqttPacketCodec) {
    super(connection, updateActivityFunction, validPacketHandler, invalidPacketHandler, maxPacketsByRead);
    this.mqttPacketCodec = mqttPacketCodec;
  }

  @Override
  protected boolean canStartReadPacket(ByteBuffer buffer) {
    return buffer.remaining() >= PACKET_LENGTH_START_BYTE;
  }

  @Override
  protected int readFullPacketLength(ByteBuffer buffer) {
    return mqttPacketCodec.decodePacketLength(buffer);
  }

  @Nullable
  @Override
  protected MqttInMessage createPacketFor(
      ByteBuffer buffer,
      int startPacketPosition,
      int packetLength,
      int dataLength) {
    return mqttPacketCodec.decodePacket(buffer, startPacketPosition);
  }
}
