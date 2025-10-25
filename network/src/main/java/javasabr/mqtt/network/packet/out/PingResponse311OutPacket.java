package javasabr.mqtt.network.packet.out;

import javasabr.mqtt.network.message.MqttMessageType;

/**
 * PING response.
 */
public class PingResponse311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) MqttMessageType.PING_RESPONSE.ordinal();

  @Override
  protected byte messageType() {
    return PACKET_TYPE;
  }
}
