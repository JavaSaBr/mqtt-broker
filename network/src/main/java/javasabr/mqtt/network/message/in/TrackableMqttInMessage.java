package javasabr.mqtt.network.message.in;

import java.nio.ByteBuffer;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.network.MqttConnection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class TrackableMqttInMessage extends MqttInMessage {

  static {
    DebugUtils.registerIncludedFields("messageId");
  }

  int messageId;

  public TrackableMqttInMessage(byte messageFlags) {
    super(messageFlags);
    this.messageId = MqttProperties.MESSAGE_ID_IS_NOT_SET;
  }

  @Override
  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    messageId = readShortUnsigned(buffer);
  }
}
