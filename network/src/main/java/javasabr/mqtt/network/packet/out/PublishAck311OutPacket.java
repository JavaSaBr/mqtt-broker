package javasabr.mqtt.network.packet.out;

import java.nio.ByteBuffer;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.packet.MqttPacketType;
import lombok.RequiredArgsConstructor;

/**
 * Publish acknowledgement.
 */
@RequiredArgsConstructor
public class PublishAck311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) MqttPacketType.PUBLISH_ACK.ordinal();

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
  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718045
    buffer.putShort((short) packetId);
  }
}
