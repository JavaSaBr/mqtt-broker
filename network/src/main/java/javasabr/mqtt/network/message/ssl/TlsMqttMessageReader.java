package javasabr.mqtt.network.message.ssl;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttPacketCodec;
import javasabr.mqtt.network.message.in.MqttInMessage;
import javasabr.rlib.network.packet.WritableNetworkPacket;
import javasabr.rlib.network.packet.impl.AbstractSslNetworkPacketReader;
import javax.net.ssl.SSLEngine;
import lombok.CustomLog;
import org.jspecify.annotations.Nullable;

@CustomLog
public class TlsMqttMessageReader
    extends AbstractSslNetworkPacketReader<MqttInMessage, MqttConnection> {

  private static final int PACKET_LENGTH_POSITION = 2;

  private final MqttPacketCodec mqttPacketCodec;

  public TlsMqttMessageReader(
      MqttConnection connection,
      Runnable updateActivityFunction,
      Consumer<? super MqttInMessage> validPacketHandler,
      Consumer<? super MqttInMessage> invalidPacketHandler,
      SSLEngine sslEngine,
      Consumer<WritableNetworkPacket<MqttConnection>> packetWriter,
      int maxPacketsByRead,
      MqttPacketCodec mqttPacketCodec) {
    super(
        connection,
        updateActivityFunction,
        validPacketHandler,
        invalidPacketHandler,
        sslEngine,
        packetWriter,
        maxPacketsByRead);
    this.mqttPacketCodec = mqttPacketCodec;
  }

  @Override
  protected boolean canStartReadPacket(ByteBuffer buffer) {
    return buffer.remaining() >= PACKET_LENGTH_POSITION;
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
