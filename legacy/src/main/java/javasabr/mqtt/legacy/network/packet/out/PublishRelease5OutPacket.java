package javasabr.mqtt.legacy.network.packet.out;

import javasabr.mqtt.legacy.model.PacketProperty;
import javasabr.mqtt.legacy.model.data.type.StringPair;
import javasabr.mqtt.legacy.model.reason.code.PublishReleaseReasonCode;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.rlib.collections.array.Array;

/**
 * Publish release (QoS 2 delivery part 2).
 */
public class PublishRelease5OutPacket extends PublishRelease311OutPacket {

  private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
        /*
          Followed by the UTF-8 Encoded String representing the reason associated with this response. This
          Reason String is human readable, designed for diagnostics and SHOULD NOT be parsed by the
          receiver.

          The sender uses this value to give additional information to the receiver. The sender MUST NOT send
          this Property if it would increase the size of the PUBREL packet beyond the Maximum Packet Size
          specified by the receiver [MQTT-3.6.2-2]. It is a Protocol Error to include the Reason String more than
          once.
         */
      PacketProperty.REASON_STRING,
        /*
          Followed by UTF-8 String Pair. This property can be used to provide additional diagnostic or other
          information for the PUBREL. The sender MUST NOT send this property if it would increase the size of the
          PUBREL packet beyond the Maximum Packet Size specified by the receiver [MQTT-3.6.2-3]. The User
          Property is allowed to appear multiple times to represent multiple name, value pairs. The same name is
          allowed to appear more than once
         */
      PacketProperty.USER_PROPERTY);

  private final Array<StringPair> userProperties;
  private final PublishReleaseReasonCode reasonCode;
  private final String reason;

  public PublishRelease5OutPacket(
      int packetId,
      PublishReleaseReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason) {
    super(packetId);
    this.userProperties = userProperties;
    this.reasonCode = reasonCode;
    this.reason = reason;
  }

  @Override
  public int getExpectedLength() {
    return -1;
  }

  @Override
  protected void writeVariableHeader(ByteBuffer buffer) {
    super.writeVariableHeader(buffer);

    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901144
    writeByte(buffer, reasonCode.getValue());
  }

  @Override
  protected boolean isPropertiesSupported() {
    return true;
  }

  @Override
  protected void writeProperties(ByteBuffer buffer) {
    super.writeProperties(buffer);

    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901145
    writeStringPairProperties(buffer, PacketProperty.USER_PROPERTY, userProperties);
    writeNotEmptyProperty(buffer, PacketProperty.REASON_STRING, reason);
  }
}
