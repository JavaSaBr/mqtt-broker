package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttMessageType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Publish release (QoS 2 delivery part 2).
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class PublishReleaseMqtt311OutMessage extends MqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.PUBLISH_RELEASED.ordinal();

  int messageId;

  @Override
  protected byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  protected byte packetFlags() {
    return 2;
  }

  @Override
  public int expectedLength(MqttConnection connection) {
    return PACKET_ID_SIZE;
  }

  @Override
  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718055
    writeShort(buffer, messageId);
  }
}
