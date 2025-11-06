package javasabr.mqtt.network.message.out;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttMessageType;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Publish acknowledgement.
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class PublishAckMqtt311OutMessage extends TrackableMqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.PUBLISH_ACK.ordinal();

  public PublishAckMqtt311OutMessage(int messageId) {
    super(messageId);
  }

  /**
   * Packet Identifier from the PUBLISH packet that is being acknowledged.
   * {@link TrackableMqttOutMessage#messageId}
   */
  //int messageId;

  @Override
  public int expectedLength(MqttConnection connection) {
    return 2;
  }

  @Override
  protected byte messageType() {
    return MESSAGE_TYPE;
  }
}
