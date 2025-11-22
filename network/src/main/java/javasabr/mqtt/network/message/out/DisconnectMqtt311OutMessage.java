package javasabr.mqtt.network.message.out;

import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.network.MqttConnection;

/**
 * Disconnect notification.
 */
public class DisconnectMqtt311OutMessage extends MqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.DISCONNECT.ordinal();

  @Override
  public int expectedLength(MqttConnection connection) {
    return 0;
  }

  @Override
  protected byte messageType() {
    return MESSAGE_TYPE;
  }
}
