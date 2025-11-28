package javasabr.mqtt.network.message.out;

import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.network.MqttConnection;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Publish received (QoS 2 delivery part 1).
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class PublishReceivedMqtt311OutMessage extends TrackableMqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.PUBLISH_RECEIVED.ordinal();

  public PublishReceivedMqtt311OutMessage(int messageId) {
    super(messageId);
  }

  @Override
  public int expectedLength(MqttConnection connection) {
    return 2;
  }

  @Override
  protected byte messageTypeId() {
    return MESSAGE_TYPE;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_RECEIVED;
  }
}
