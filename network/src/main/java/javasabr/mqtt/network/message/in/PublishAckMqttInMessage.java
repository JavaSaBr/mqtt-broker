package javasabr.mqtt.network.message.in;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.TrackableMessage;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Publish acknowledgment (QoS 1).
 */
@Getter
@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublishAckMqttInMessage extends MqttInMessage implements TrackableMessage {

  private static final int MESSAGE_TYPE = MqttMessageType.PUBLISH_ACK.ordinal();

  static {
    DebugUtils.registerIncludedFields("reasonCode", "messageId");
  }

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

  PublishAckReasonCode reasonCode;
  int messageId;

  // properties
  String reason;

  public PublishAckMqttInMessage(byte messageFlags) {
    super(messageFlags);
    this.reasonCode = PublishAckReasonCode.SUCCESS;
    this.reason = "";
  }

  @Override
  public byte messageType() {
    return (byte) MESSAGE_TYPE;
  }

  @Override
  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {

    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718045
    messageId = readShortUnsigned(buffer);

    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901123
    if (connection.isSupported(MqttVersion.MQTT_5) && buffer.hasRemaining()) {
      reasonCode = PublishAckReasonCode.ofCode(readByteUnsigned(buffer));
    }
  }

  @Override
  protected boolean isPropertiesSupported(MqttConnection connection, ByteBuffer buffer) {
    return connection.isSupported(MqttVersion.MQTT_5) && buffer.hasRemaining();
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
