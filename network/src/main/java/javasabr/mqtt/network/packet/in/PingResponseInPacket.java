package javasabr.mqtt.network.packet.in;

import javasabr.mqtt.network.packet.PacketType;

/**
 * PING response.
 */
public class PingResponseInPacket extends MqttReadablePacket {

  public static final byte PACKET_TYPE = (byte) PacketType.PING_RESPONSE.ordinal();

  public PingResponseInPacket(byte info) {
    super(info);
  }

  @Override
  public byte packetType() {
    return PACKET_TYPE;
  }
}
