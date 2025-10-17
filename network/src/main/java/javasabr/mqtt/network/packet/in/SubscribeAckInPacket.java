package javasabr.mqtt.network.packet.in;

import javasabr.mqtt.model.PacketProperty;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.packet.PacketType;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.common.util.StringUtils;
import lombok.Getter;

/**
 * Subscribe acknowledgement.
 */
@Getter
public class SubscribeAckInPacket extends MqttReadablePacket {

  private static final byte PACKET_TYPE = (byte) PacketType.SUBSCRIBE_ACK.ordinal();

  private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
        /*
          Followed by the UTF-8 Encoded String representing the reason associated with this response. This
          Reason String is a human readable string designed for diagnostics and SHOULD NOT be parsed by the
          Client.

          The Server uses this value to give additional information to the Client. The Server MUST NOT send this
          Property if it would increase the size of the SUBACK packet beyond the Maximum Packet Size specified
          by the Client
         */
      PacketProperty.REASON_STRING,
        /*
          Followed by UTF-8 String Pair. This property can be used to provide additional diagnostic or other
          information. The Server MUST NOT send this property if it would increase the size of the SUBACK packet
          beyond the Maximum Packet Size specified by Client [MQTT-3.9.2-2]. The User Property is allowed to
          appear multiple times to represent multiple name, value pairs. The same name is allowed to appear more
          than once.
         */
      PacketProperty.USER_PROPERTY);

  private MutableArray<SubscribeAckReasonCode> reasonCodes;
  private int packetId;

  // properties
  private String reason;

  public SubscribeAckInPacket(byte info) {
    super(info);
    this.reasonCodes = ArrayFactory.mutableArray(SubscribeAckReasonCode.class);
    this.reason = StringUtils.EMPTY;
  }

  @Override
  public byte packetType() {
    return PACKET_TYPE;
  }

  @Override
  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718070
    packetId = readUnsignedShort(buffer);
  }

  @Override
  protected void readPayload(MqttConnection connection, ByteBuffer buffer) {

    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718071
    if (buffer.remaining() < 1) {
      throw new IllegalStateException("No any topic filters.");
    }

    while (buffer.hasRemaining()) {
      reasonCodes.add(SubscribeAckReasonCode.of(readUnsignedByte(buffer)));
    }
  }

  @Override
  protected Set<PacketProperty> getAvailableProperties() {
    return AVAILABLE_PROPERTIES;
  }

  @Override
  protected void applyProperty(PacketProperty property, String value) {
    switch (property) {
      case REASON_STRING:
        reason = value;
        break;
      default:
        unexpectedProperty(property);
    }
  }
}
