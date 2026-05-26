package javasabr.mqtt.network.message.plain;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttPacketCreator;
import javasabr.mqtt.network.message.in.MqttInMessage;
import javasabr.rlib.network.packet.impl.AbstractNetworkPacketReader;
import lombok.CustomLog;
import org.jspecify.annotations.Nullable;

@CustomLog
public class PlainMqttMessageReader extends AbstractNetworkPacketReader<MqttInMessage, MqttConnection> {

  private static final int PACKET_LENGTH_START_BYTE = 2;

  private final MqttPacketCreator mqttPacketCreator;

  public PlainMqttMessageReader(
      MqttConnection connection,
      Runnable updateActivityFunction,
      Consumer<MqttInMessage> validPacketHandler,
      Consumer<MqttInMessage> invalidPacketHandler,
      int maxPacketsByRead,
      MqttPacketCreator mqttPacketCreator) {
    super(connection, updateActivityFunction, validPacketHandler, invalidPacketHandler, maxPacketsByRead);
    this.mqttPacketCreator = mqttPacketCreator;
  }

  @Override
  protected boolean canStartReadPacket(ByteBuffer buffer) {
    return buffer.remaining() >= PACKET_LENGTH_START_BYTE;
  }

  @Override
  protected int readFullPacketLength(ByteBuffer buffer) {
    return mqttPacketCreator.readFullPacketLength(buffer);
  }

  @Nullable
  @Override
  protected MqttInMessage createPacketFor(
      ByteBuffer buffer,
      int startPacketPosition,
      int packetLength,
      int dataLength) {
    return mqttPacketCreator.createPacketFor(buffer, startPacketPosition);
  }
}
