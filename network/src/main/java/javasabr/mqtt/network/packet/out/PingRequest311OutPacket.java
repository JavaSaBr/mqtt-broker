package javasabr.mqtt.network.packet.out;

import javasabr.mqtt.network.packet.PacketType;

/**
 * PING request.
 */
public class PingRequest311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) PacketType.PING_REQUEST.ordinal();

  @Override
  protected byte getPacketType() {
    return PACKET_TYPE;
  }
}
