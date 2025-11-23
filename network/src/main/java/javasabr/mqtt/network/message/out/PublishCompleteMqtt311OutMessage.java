package javasabr.mqtt.network.message.out;

import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.network.MqttConnection;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Publish complete (QoS 2 delivery part 3).
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class PublishCompleteMqtt311OutMessage extends TrackableMqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.PUBLISH_COMPLETE.ordinal();

  public PublishCompleteMqtt311OutMessage(int messageId) {
    super(messageId);
  }

  @Override
  public int expectedLength(MqttConnection connection) {
    return 2;
  }

  @Override
  protected byte messageType() {
    return MESSAGE_TYPE;
  }
}
