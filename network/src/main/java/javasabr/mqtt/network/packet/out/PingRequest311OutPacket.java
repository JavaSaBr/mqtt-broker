package javasabr.mqtt.network.packet.out;

import javasabr.mqtt.network.packet.MqttPacketType;

/**
 * PING request.
 */
public class PingRequest311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) MqttPacketType.PING_REQUEST.ordinal();

  @Override
  protected byte packetType() {
    return PACKET_TYPE;
  }
}
