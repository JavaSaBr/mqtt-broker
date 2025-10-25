package javasabr.mqtt.network.packet.out;

import java.nio.ByteBuffer;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttMessageType;
import lombok.RequiredArgsConstructor;

/**
 * Unsubscribe acknowledgement.
 */
@RequiredArgsConstructor
public class UnsubscribeAck311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) MqttMessageType.UNSUBSCRIBE_ACK.ordinal();

  private final int packetId;

  @Override
  public int expectedLength(MqttConnection connection) {
    return 2;
  }

  @Override
  protected byte messageType() {
    return PACKET_TYPE;
  }

  @Override
  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718074
    writeShort(buffer, packetId);
  }
}
