package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Unsubscribe acknowledgement.
 */
@Getter
@Accessors
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UnsubscribeAckMqtt5OutMessage extends UnsubscribeAckMqtt311OutMessage {

  private static final Set<MqttMessageProperty> AVAILABLE_PROPERTIES = EnumSet.of(
      /*
        Followed by the UTF-8 Encoded String representing the reason associated with this response. This
        Reason String is a human readable string designed for diagnostics and SHOULD NOT be parsed by the
        Client.

        The Server uses this value to give additional information to the Client. The Server MUST NOT send this
        Property if it would increase the size of the UNSUBACK packet beyond the Maximum Packet Size
        specified by the Client [MQTT-3.11.2-1]. It is a Protocol Error to include the Reason String more than
        once.
       */
      MqttMessageProperty.REASON_STRING,
      /*
        Followed by UTF-8 String Pair. This property can be used to provide additional diagnostic or other
        information. The Server MUST NOT send this property if it would increase the size of the UNSUBACK
        packet beyond the Maximum Packet Size specified by the Client [MQTT-3.11.2-2]. The User Property is
        allowed to appear multiple times to represent multiple name, value pairs. The same name is allowed to
        appear more than once.
       */
      MqttMessageProperty.USER_PROPERTY);

  Array<UnsubscribeAckReasonCode> reasonCodes;
  Array<StringPair> userProperties;
  String reason;

  public UnsubscribeAckMqtt5OutMessage(
      int messageId,
      Array<UnsubscribeAckReasonCode> reasonCodes,
      Array<StringPair> userProperties,
      String reason) {
    super(messageId);
    this.reasonCodes = reasonCodes;
    this.userProperties = userProperties;
    this.reason = reason;
  }

  @Override
  protected boolean isPropertiesSupported(MqttConnection connection) {
    return true;
  }

  @Override
  protected void writeProperties(MqttConnection connection, ByteBuffer buffer) {
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901182
    writeStringPairProperties(buffer, MqttMessageProperty.USER_PROPERTY, userProperties);
    writeNotEmptyProperty(buffer, MqttMessageProperty.REASON_STRING, reason);
  }

  @Override
  protected void writePayload(MqttConnection connection, ByteBuffer buffer) {
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901185
    for (var reasonCode : reasonCodes) {
      writeByte(buffer, reasonCode.code());
    }
  }
}
