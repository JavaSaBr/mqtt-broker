package javasabr.mqtt.network.packet.out;

import java.nio.ByteBuffer;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.packet.PacketType;
import lombok.RequiredArgsConstructor;

/**
 * Publish release (QoS 2 delivery part 2).
 */
@RequiredArgsConstructor
public class PublishRelease311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) PacketType.PUBLISH_RELEASED.ordinal();

  private final int packetId;

  @Override
  protected byte packetType() {
    return PACKET_TYPE;
  }

  @Override
  protected byte packetFlags() {
    return 2;
  }

  @Override
  public int expectedLength(MqttConnection connection) {
    return PACKET_ID_SIZE;
  }

  @Override
  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718055
    writeShort(buffer, packetId);
  }
}
