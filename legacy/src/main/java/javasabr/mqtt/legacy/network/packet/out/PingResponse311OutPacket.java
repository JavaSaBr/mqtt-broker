package javasabr.mqtt.legacy.network.packet.out;

import javasabr.mqtt.legacy.network.packet.PacketType;

/**
 * PING response.
 */
public class PingResponse311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) PacketType.PING_RESPONSE.ordinal();

  @Override
  protected byte getPacketType() {
    return PACKET_TYPE;
  }
}
