package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Publish acknowledgement.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishAckMqtt5OutMessage extends PublishAckMqtt311OutMessage {

  private static final Set<MqttMessageProperty> AVAILABLE_PROPERTIES = EnumSet.of(
      /*
        Followed by the UTF-8 Encoded String representing the reason associated with this response. This
        Reason String is a human readable string designed for diagnostics and is not intended to be parsed by
        the receiver.

        The sender uses this value to give additional information to the receiver. The sender MUST NOT send
        this property if it would increase the size of the PUBACK packet beyond the Maximum Packet Size
        specified by the receiver [MQTT-3.4.2-2]. It is a Protocol Error to include the Reason String more than
        once.
       */
      MqttMessageProperty.REASON_STRING,
      /*
        Followed by UTF-8 String Pair. This property can be used to provide additional diagnostic or other
        information. The sender MUST NOT send this property if it would increase the size of the PUBACK
        packet beyond the Maximum Packet Size specified by the receiver [MQTT-3.4.2-3]. The User Property is
        allowed to appear multiple times to represent multiple name, value pairs. The same name is allowed to
        appear more than once.
       */
      MqttMessageProperty.USER_PROPERTY);

  Array<StringPair> userProperties;
  String reason;
  PublishAckReasonCode reasonCode;

  public PublishAckMqtt5OutMessage(
      int messageId,
      PublishAckReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason) {
    super(messageId);
    this.reasonCode = reasonCode;
    this.userProperties = userProperties;
    this.reason = reason;
  }

  @Override
  protected boolean isPropertiesSupported(MqttConnection connection) {
    return true;
  }

  @Override
  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    super.writeVariableHeader(connection, buffer);
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901123
    writeByte(buffer, reasonCode.value());
  }

  @Override
  protected void writeProperties(MqttConnection connection, ByteBuffer buffer) {
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901125
    writeStringPairProperties(buffer, MqttMessageProperty.USER_PROPERTY, userProperties);
    writeNotEmptyProperty(buffer, MqttMessageProperty.REASON_STRING, reason);
  }
}
