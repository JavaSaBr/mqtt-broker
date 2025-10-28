package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Subscribe acknowledgement.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubscribeAckMqtt5OutMessage extends SubscribeAckMqtt311OutMessage {

  static {
    DebugUtils.registerIncludedFields("reasonCodes", "messageId");
  }

  private static final Set<MqttMessageProperty> AVAILABLE_PROPERTIES = EnumSet.of(
      /*
        Followed by the UTF-8 Encoded String representing the reason associated with this response. This
        Reason String is a human readable string designed for diagnostics and SHOULD NOT be parsed by the
        Client.

        The Server uses this value to give additional information to the Client. The Server MUST NOT send this
        Property if it would increase the size of the SUBACK packet beyond the Maximum Packet Size specified
        by the Client
       */
      MqttMessageProperty.REASON_STRING,
      /*
        Followed by UTF-8 String Pair. This property can be used to provide additional diagnostic or other
        information. The Server MUST NOT send this property if it would increase the size of the SUBACK packet
        beyond the Maximum Packet Size specified by Client [MQTT-3.9.2-2]. The User Property is allowed to
        appear multiple times to represent multiple name, value pairs. The same name is allowed to appear more
        than once.
       */
      MqttMessageProperty.USER_PROPERTY);

  Array<StringPair> userProperties;
  String reason;

  public SubscribeAckMqtt5OutMessage(
      int messageId,
      Array<SubscribeAckReasonCode> reasonCodes,
      Array<StringPair> userProperties,
      String reason) {
    super(reasonCodes, messageId);
    this.userProperties = userProperties;
    this.reason = reason;
  }

  @Override
  public int expectedLength(MqttConnection connection) {
    return UNKNOWN_EXPECTED_BYTES;
  }

  @Override
  protected boolean isPropertiesSupported(MqttConnection connection) {
    return true;
  }

  @Override
  protected void writeProperties(MqttConnection connection, ByteBuffer buffer) {

    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901174
    writeStringPairProperties(buffer, MqttMessageProperty.USER_PROPERTY, userProperties);
    writeNotEmptyProperty(buffer, MqttMessageProperty.REASON_STRING, reason);
  }
}
