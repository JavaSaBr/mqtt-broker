package javasabr.mqtt.network.packet.out;

import javasabr.mqtt.network.message.MqttMessageType;

/**
 * PING request.
 */
public class PingRequest311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) MqttMessageType.PING_REQUEST.ordinal();

  @Override
  protected byte messageType() {
    return PACKET_TYPE;
  }
}
