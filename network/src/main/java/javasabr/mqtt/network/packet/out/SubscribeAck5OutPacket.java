package javasabr.mqtt.network.packet.out;

import javasabr.mqtt.model.PacketProperty;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.base.utils.DebugUtils;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.rlib.collections.array.Array;

/**
 * Subscribe acknowledgement.
 */
public class SubscribeAck5OutPacket extends SubscribeAck311OutPacket {

  static {
    DebugUtils.registerIncludedFields("reasonCodes", "packetId");
  }

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

  private final Array<StringPair> userProperties;
  private final String reason;

  public SubscribeAck5OutPacket(
      int packetId,
      Array<SubscribeAckReasonCode> reasonCodes,
      Array<StringPair> userProperties,
      String reason) {
    super(reasonCodes, packetId);
    this.userProperties = userProperties;
    this.reason = reason;
  }

  @Override
  protected boolean isPropertiesSupported() {
    return true;
  }

  @Override
  protected void writeProperties(ByteBuffer buffer) {

    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901174
    writeStringPairProperties(buffer, PacketProperty.USER_PROPERTY, userProperties);
    writeNotEmptyProperty(buffer, PacketProperty.REASON_STRING, reason);
  }
}
