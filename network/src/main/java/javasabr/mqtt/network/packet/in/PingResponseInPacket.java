package javasabr.mqtt.network.packet.in;

import javasabr.mqtt.network.packet.MqttPacketType;

/**
 * PING response.
 */
public class PingResponseInPacket extends MqttReadablePacket {

  public static final byte PACKET_TYPE = (byte) MqttPacketType.PING_RESPONSE.ordinal();

  public PingResponseInPacket(byte info) {
    super(info);
  }

  @Override
  public byte packetType() {
    return PACKET_TYPE;
  }
}
