package javasabr.mqtt.network.message.in;

import java.nio.ByteBuffer;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.TrackableMessage;
import javasabr.mqtt.model.reason.code.ReasonCode;
import javasabr.mqtt.network.MqttConnection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@Getter
@Accessors
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class PublishControlMqttInMessage<R extends ReasonCode> extends TrackableMqttInMessage
    implements TrackableMessage {

  public static final byte MESSAGE_FLAGS = 0b0000_0000;

  static {
    DebugUtils.registerIncludedFields("reasonCode");
  }

  R reasonCode;
  // properties
  @Nullable
  String reason;

  public PublishControlMqttInMessage(byte messageFlags) {
    super(messageFlags);
    this.reasonCode = defaultReasonCode();
  }

  @Override
  protected boolean validMessageFlags(byte messageFlags) {
    return messageFlags == MESSAGE_FLAGS;
  }

  protected abstract R defaultReasonCode();

  protected abstract R readReasonCode(int unsignedByte);

  @Override
  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    super.readVariableHeader(connection, buffer);
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901123
    if (connection.isSupported(MqttVersion.MQTT_5) && buffer.hasRemaining()) {
      reasonCode = readReasonCode(readByteUnsigned(buffer));
    }
  }

  @Override
  protected boolean isPropertiesSupported(MqttConnection connection, ByteBuffer buffer) {
    return connection.isSupported(MqttVersion.MQTT_5) && buffer.hasRemaining();
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
