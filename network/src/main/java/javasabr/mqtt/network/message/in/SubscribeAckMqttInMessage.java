package javasabr.mqtt.network.message.in;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.MqttProtocolErrors;
import javasabr.mqtt.model.exception.MalformedProtocolMqttException;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

/**
 * Subscribe acknowledgement.
 */
@Getter
@Accessors
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubscribeAckMqttInMessage extends TrackableMqttInMessage {

  public static final byte MESSAGE_FLAGS = 0b0000_0000;
  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.SUBSCRIBE_ACK.ordinal();

  static {
    DebugUtils.registerIncludedFields("reasonCodes");
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

  @Nullable
  MutableArray<SubscribeAckReasonCode> reasonCodes;

  // properties
  @Nullable
  String reason;

  public SubscribeAckMqttInMessage(byte messageFlags) {
    super(messageFlags);
  }

  @Override
  public byte messageTypeId() {
    return MESSAGE_TYPE;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.SUBSCRIBE_ACK;
  }
  
  @Override
  protected boolean validMessageFlags(byte messageFlags) {
    return messageFlags == MESSAGE_FLAGS;
  }

  @Override
  protected void readPayload(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718071
    if (!buffer.hasRemaining()) {
      throw new MalformedProtocolMqttException(MqttProtocolErrors.NO_ANY_TOPIC_FILTERS);
    }
    reasonCodes = ArrayFactory.mutableArray(SubscribeAckReasonCode.class, buffer.remaining());
    while (buffer.hasRemaining()) {
      reasonCodes.add(SubscribeAckReasonCode.ofCode(readByteUnsigned(buffer)));
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
      case REASON_STRING -> {
        if (reason != null) {
          alreadyPresentedProperty(property);
        }
        reason = value;
      }
      default -> unsupportedProperty(property);
    }
  }
}
