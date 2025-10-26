package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.model.PacketProperty;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Publish complete (QoS 2 delivery part 3).
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishCompleteMqtt5OutMessage extends PublishCompleteMqtt311OutMessage {

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

  Array<StringPair> userProperties;
  PublishCompletedReasonCode reasonCode;
  String reason;

  public PublishCompleteMqtt5OutMessage(int messageId, PublishCompletedReasonCode reasonCode) {
    this(messageId, reasonCode, Array.empty(StringPair.class), StringUtils.EMPTY);
  }

  public PublishCompleteMqtt5OutMessage(
      int messageId,
      PublishCompletedReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason) {
    super(messageId);
    this.reasonCode = reasonCode;
    this.userProperties = userProperties;
    this.reason = reason;
  }

  @Override
  public int expectedLength(MqttConnection connection) {
    return UNKNOWN_EXPECTED_BYTES;
  }

  @Override
  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    super.writeVariableHeader(connection, buffer);
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901154
    writeByte(buffer, reasonCode.getValue());
  }

  @Override
  protected boolean isPropertiesSupported(MqttConnection connection) {
    return true;
  }

  @Override
  protected void writeProperties(MqttConnection connection, ByteBuffer buffer) {
    super.writeProperties(connection, buffer);
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901155
    writeStringPairProperties(buffer, PacketProperty.USER_PROPERTY, userProperties);
    writeNotEmptyProperty(buffer, PacketProperty.REASON_STRING, reason);
  }
}
