package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.model.PacketProperty;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Publish release (QoS 2 delivery part 2).
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishReleaseMqtt5OutMessage extends PublishReleaseMqtt311OutMessage {

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
  PublishReleaseReasonCode reasonCode;
  String reason;

  public PublishReleaseMqtt5OutMessage(
      int messageId,
      PublishReleaseReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason) {
    super(messageId);
    this.userProperties = userProperties;
    this.reasonCode = reasonCode;
    this.reason = reason;
  }

  @Override
  public int expectedLength(MqttConnection connection) {
    return UNKNOWN_EXPECTED_BYTES;
  }

  @Override
  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    super.writeVariableHeader(connection, buffer);
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901144
    writeByte(buffer, reasonCode.getValue());
  }

  @Override
  protected boolean isPropertiesSupported(MqttConnection connection) {
    return true;
  }

  @Override
  protected void writeProperties(MqttConnection connection, ByteBuffer buffer) {
    super.writeProperties(connection, buffer);
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901145
    writeStringPairProperties(buffer, PacketProperty.USER_PROPERTY, userProperties);
    writeNotEmptyProperty(buffer, PacketProperty.REASON_STRING, reason);
  }
}
