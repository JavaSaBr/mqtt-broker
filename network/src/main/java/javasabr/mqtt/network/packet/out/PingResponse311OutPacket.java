package javasabr.mqtt.network.packet.out;

import javasabr.mqtt.network.packet.PacketType;

/**
 * PING response.
 */
public class PingResponse311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) PacketType.PING_RESPONSE.ordinal();

  @Override
  protected byte packetType() {
    return PACKET_TYPE;
  }
}
