package javasabr.mqtt.network.packet.out;

import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.packet.PacketType;
import javasabr.mqtt.base.utils.DebugUtils;
import java.nio.ByteBuffer;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Subscribe acknowledgement.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubscribeAck311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) PacketType.SUBSCRIBE_ACK.ordinal();

  static {
    DebugUtils.registerIncludedFields("reasonCodes", "packetId");
  }

  /**
   * The order of Reason Codes in the SUBACK packet MUST match the order of Topic Filters in the SUBSCRIBE packet.
   */
  Array<SubscribeAckReasonCode> reasonCodes;

  /**
   * The Packet Identifier from the SUBSCRIBE.
   */
  int packetId;

  @Override
  public int expectedLength(MqttConnection connection) {
    return 2 + reasonCodes.size();
  }

  @Override
  protected byte packetType() {
    return PACKET_TYPE;
  }

  @Override
  protected void writeVariableHeader(ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718070
    writeShort(buffer, packetId);
  }

  @Override
  protected void writePayload(ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718071
    for (var reasonCode : reasonCodes) {
      writeByte(buffer, reasonCode.getValue());
    }
  }
}
