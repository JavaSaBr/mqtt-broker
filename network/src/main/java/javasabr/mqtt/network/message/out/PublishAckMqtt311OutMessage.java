package javasabr.mqtt.network.message.out;

import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.network.MqttConnection;

/**
 * Publish acknowledgement.
 */
public class PublishAckMqtt311OutMessage extends TrackableMqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.PUBLISH_ACK.ordinal();

  public PublishAckMqtt311OutMessage(int messageId) {
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
    return MqttMessageType.PUBLISH_ACK;
  }
}
