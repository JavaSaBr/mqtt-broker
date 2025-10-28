package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.network.MqttConnection;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class TrackableMqttOutMessage extends MqttOutMessage {

  static {
    DebugUtils.registerIncludedFields("messageId");
  }

  int messageId;

  @Override
  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    writeShort(buffer, messageId);
  }
}
