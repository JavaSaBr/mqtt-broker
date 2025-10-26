package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttMessageType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Publish acknowledgement.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class PublishAckMqtt311OutMessage extends MqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.PUBLISH_ACK.ordinal();

  /**
   * Packet Identifier from the PUBLISH packet that is being acknowledged.
   */
  int messageId;

  @Override
  public int expectedLength(MqttConnection connection) {
    return 2;
  }

  @Override
  protected byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718045
    buffer.putShort((short) messageId);
  }
}
