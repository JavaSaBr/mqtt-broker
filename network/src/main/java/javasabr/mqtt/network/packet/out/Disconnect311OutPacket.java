package javasabr.mqtt.network.packet.out;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttMessageType;

/**
 * Disconnect notification.
 */
public class Disconnect311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) MqttMessageType.DISCONNECT.ordinal();

  @Override
  public int expectedLength(MqttConnection connection) {
    return 0;
  }

  @Override
  protected byte messageType() {
    return PACKET_TYPE;
  }
}
