package javasabr.mqtt.legacy.network.packet.out;

import javasabr.mqtt.legacy.network.packet.PacketType;

/**
 * Disconnect notification.
 */
public class Disconnect311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) PacketType.DISCONNECT.ordinal();

  @Override
  public int getExpectedLength() {
    return 0;
  }

  @Override
  protected byte getPacketType() {
    return PACKET_TYPE;
  }
}
