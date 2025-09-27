package javasabr.mqtt.legacy.network.packet.in;

import javasabr.mqtt.legacy.network.packet.PacketType;

/**
 * PING response.
 */
public class PingResponseInPacket extends MqttReadablePacket {

  public static final byte PACKET_TYPE = (byte) PacketType.PING_RESPONSE.ordinal();

  public PingResponseInPacket(byte info) {
    super(info);
  }

  @Override
  public byte getPacketType() {
    return PACKET_TYPE;
  }
}
