package javasabr.mqtt.network.message.in;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.TrackableMessage;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode;
import javasabr.mqtt.network.MqttConnection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

/**
 * Publish complete (QoS 2 delivery part 3).
 */
@Getter
@Accessors
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublishCompleteMqttInMessage extends TrackableMqttInMessage implements TrackableMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.PUBLISH_COMPLETE.ordinal();

  static {
    DebugUtils.registerIncludedFields("reasonCode", "messageId");
  }

  private static final Set<MqttMessageProperty> AVAILABLE_PROPERTIES = EnumSet.of(
      /*
        Followed by the UTF-8 Encoded String representing the reason associated with this response. This
        Reason String is human readable, designed for diagnostics and SHOULD NOT be parsed by the
        receiver.

        The sender uses this value to give additional information to the receiver. The sender MUST NOT send
        this Property if it would increase the size of the PUBREL packet beyond the Maximum Packet Size
        specified by the receiver [MQTT-3.6.2-2]. It is a Protocol Error to include the Reason String more than
        once.
       */
      MqttMessageProperty.REASON_STRING,
      /*
        Followed by UTF-8 String Pair. This property can be used to provide additional diagnostic or other
        information for the PUBREL. The sender MUST NOT send this property if it would increase the size of the
        PUBREL packet beyond the Maximum Packet Size specified by the receiver [MQTT-3.6.2-3]. The User
        Property is allowed to appear multiple times to represent multiple name, value pairs. The same name is
        allowed to appear more than once
       */
      MqttMessageProperty.USER_PROPERTY);

  PublishCompletedReasonCode reasonCode;

  // properties
  @Nullable
  String reason;

  public PublishCompleteMqttInMessage(byte messageFlags) {
    super(messageFlags);
    this.reasonCode = PublishCompletedReasonCode.SUCCESS;
  }

  @Override
  public byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  protected boolean validMessageFlags(byte messageFlags) {
    return messageFlags == 0b0000_0000;
  }

  @Override
  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    super.readVariableHeader(connection, buffer);
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901154
    if (connection.isSupported(MqttVersion.MQTT_5) && buffer.hasRemaining()) {
      reasonCode = PublishCompletedReasonCode.ofCode(readByteUnsigned(buffer));
    }
  }

  @Override
  protected boolean isPropertiesSupported(MqttConnection connection, ByteBuffer buffer) {
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901154
    return super.isPropertiesSupported(connection, buffer) && buffer.hasRemaining();
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
