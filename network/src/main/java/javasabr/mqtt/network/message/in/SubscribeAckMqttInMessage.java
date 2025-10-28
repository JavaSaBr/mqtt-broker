package javasabr.mqtt.network.message.in;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
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
 * Subscribe acknowledgement.
 */
@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubscribeAckMqttInMessage extends MqttInMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.SUBSCRIBE_ACK.ordinal();

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

  @Nullable
  MutableArray<SubscribeAckReasonCode> reasonCodes;
  int messageId;

  // properties
  String reason;

  public SubscribeAckMqttInMessage(byte info) {
    super(info);
    this.reason = StringUtils.EMPTY;
  }

  @Override
  public byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718070
    messageId = readShortUnsigned(buffer);
  }

  @Override
  protected void readPayload(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718071
    if (buffer.remaining() < 1) {
      throw new IllegalStateException("No any topic filters.");
    }

    reasonCodes = ArrayFactory.mutableArray(SubscribeAckReasonCode.class, buffer.remaining());
    while (buffer.hasRemaining()) {
      reasonCodes.add(SubscribeAckReasonCode.of(readByteUnsigned(buffer)));
    }
  }

  public Array<SubscribeAckReasonCode> reasonCodes() {
    return reasonCodes == null ? Array.empty(SubscribeAckReasonCode.class) : reasonCodes;
  }

  @Override
  protected Set<MqttMessageProperty> availableProperties() {
    return AVAILABLE_PROPERTIES;
  }

  @Override
  protected void applyProperty(MqttMessageProperty property, String value) {
    switch (property) {
      case REASON_STRING -> reason = value;
      default -> unexpectedProperty(property);
    }
  }
}
