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

  public TrackableMqttInMessage(byte info) {
    super(info);
    this.messageId = MqttProperties.MESSAGE_ID_UNDEFINED;
  }

  @Override
  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718065
    messageId = readShortUnsigned(buffer);
  }
}
