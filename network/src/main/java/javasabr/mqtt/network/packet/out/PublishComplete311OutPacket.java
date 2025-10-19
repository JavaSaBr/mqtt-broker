package javasabr.mqtt.network.packet.out;

import java.nio.ByteBuffer;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.packet.PacketType;
import lombok.RequiredArgsConstructor;

/**
 * Publish complete (QoS 2 delivery part 3).
 */
@RequiredArgsConstructor
public class PublishComplete311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) PacketType.PUBLISH_COMPLETED.ordinal();

  private final int packetId;

  @Override
  public int expectedLength(MqttConnection connection) {
    return 2;
  }

  @Override
  protected byte packetType() {
    return PACKET_TYPE;
  }

  @Override
  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718083
    writeShort(buffer, packetId);
  }
}
