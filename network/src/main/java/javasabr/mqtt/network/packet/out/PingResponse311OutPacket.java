package javasabr.mqtt.network.packet.out;

import javasabr.mqtt.network.packet.MqttPacketType;

/**
 * PING response.
 */
public class PingResponse311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) MqttPacketType.PING_RESPONSE.ordinal();

  @Override
  protected byte packetType() {
    return PACKET_TYPE;
  }
}
