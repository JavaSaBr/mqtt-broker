package javasabr.mqtt.network.packet.out;

import java.nio.ByteBuffer;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.packet.PacketType;
import lombok.RequiredArgsConstructor;

/**
 * Unsubscribe acknowledgement.
 */
@RequiredArgsConstructor
public class UnsubscribeAck311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) PacketType.UNSUBSCRIBE_ACK.ordinal();

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
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718074
    writeShort(buffer, packetId);
  }
}
