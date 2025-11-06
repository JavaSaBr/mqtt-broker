package javasabr.mqtt.network.message.in;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

/**
 * Unsubscribe acknowledgement.
 */
@Getter
@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UnsubscribeAckMqttInMessage extends MqttInMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.UNSUBSCRIBE_ACK.ordinal();

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

  private static final Array<UnsubscribeAckReasonCode> EMPTY_REASON_CODES = Array.empty(UnsubscribeAckReasonCode.class);

  @Nullable
  MutableArray<UnsubscribeAckReasonCode> reasonCodes;
  int messageId;

  // properties
  String reason = StringUtils.EMPTY;

  public UnsubscribeAckMqttInMessage(byte info) {
    super(info);
  }

  @Override
  public byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718079
    messageId = readShortUnsigned(buffer);
  }

  @Override
  protected void readPayload(MqttConnection connection, ByteBuffer buffer) {

    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901194
    if (!connection.isSupported(MqttVersion.MQTT_5)) {
      return;
    } else if (!buffer.hasRemaining()) {
      return;
    }

    reasonCodes = ArrayFactory.mutableArray(UnsubscribeAckReasonCode.class, buffer.remaining());
    while (buffer.hasRemaining()) {
      reasonCodes.add(UnsubscribeAckReasonCode.of(readByteUnsigned(buffer)));
    }
  }

  @Override
  protected Set<MqttMessageProperty> availableProperties() {
    return AVAILABLE_PROPERTIES;
  }

  public Array<UnsubscribeAckReasonCode> reasonCodes() {
    return reasonCodes == null ? EMPTY_REASON_CODES : reasonCodes;
  }

  @Override
  protected void applyProperty(MqttMessageProperty property, String value) {
    switch (property) {
      case REASON_STRING -> reason = value;
      default -> unexpectedProperty(property);
    }
  }
}
