package javasabr.mqtt.legacy.network.packet.in;

import javasabr.mqtt.legacy.network.packet.PacketType;

/**
 * PING request.
 */
public class PingRequestInPacket extends MqttReadablePacket {

  public static final byte PACKET_TYPE = (byte) PacketType.PING_REQUEST.ordinal();

  public PingRequestInPacket(byte info) {
    super(info);
  }

  @Override
  public byte getPacketType() {
    return PACKET_TYPE;
  }
}
