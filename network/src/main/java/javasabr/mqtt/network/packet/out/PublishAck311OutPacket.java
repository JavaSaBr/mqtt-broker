package javasabr.mqtt.network.packet.out;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.packet.PacketType;
import java.nio.ByteBuffer;
import lombok.RequiredArgsConstructor;

/**
 * Publish acknowledgement.
 */
@RequiredArgsConstructor
public class PublishAck311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) PacketType.PUBLISH_ACK.ordinal();

  /**
   * Packet Identifier from the PUBLISH packet that is being acknowledged.
   */
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
  protected void writeVariableHeader(ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718045
    buffer.putShort((short) packetId);
  }
}
