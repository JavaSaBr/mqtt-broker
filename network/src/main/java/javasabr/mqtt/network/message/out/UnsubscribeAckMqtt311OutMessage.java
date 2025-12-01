package javasabr.mqtt.network.message.out;

import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.network.MqttConnection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Unsubscribe acknowledgement.
 */
@Getter
@Accessors
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class UnsubscribeAckMqtt311OutMessage extends TrackableMqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.UNSUBSCRIBE_ACK.ordinal();

  public UnsubscribeAckMqtt311OutMessage(int messageId) {
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
    return MqttMessageType.UNSUBSCRIBE_ACK;
  }
}
