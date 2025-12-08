package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.network.MqttConnection;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Publish release (QoS 2 delivery part 2).
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class PublishReleaseMqtt311OutMessage extends TrackableMqttOutMessage {

  public static final int MESSAGE_FLAGS = 0b0000_0010;
  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.PUBLISH_RELEASE.ordinal();

  public PublishReleaseMqtt311OutMessage(int messageId) {
    super(messageId);
  }

  @Override
  protected byte messageTypeId() {
    return MESSAGE_TYPE;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_RELEASE;
  }
  
  @Override
  protected byte messageFlags() {
    return MESSAGE_FLAGS;
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
