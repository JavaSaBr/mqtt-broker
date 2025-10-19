package javasabr.mqtt.network.packet.out;

import java.nio.ByteBuffer;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.packet.PacketType;
import lombok.RequiredArgsConstructor;

/**
 * Publish received (QoS 2 delivery part 1).
 */
@RequiredArgsConstructor
public class PublishReceived311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) PacketType.PUBLISH_RECEIVED.ordinal();

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
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718050
    writeShort(buffer, packetId);
  }
}
