package javasabr.mqtt.network.packet.in;

import javasabr.mqtt.network.packet.MqttPacketType;

/**
 * PING request.
 */
public class PingRequestInPacket extends MqttReadablePacket {

  public static final byte PACKET_TYPE = (byte) MqttPacketType.PING_REQUEST.ordinal();

  public PingRequestInPacket(byte info) {
    super(info);
  }

  @Override
  public byte packetType() {
    return PACKET_TYPE;
  }
}
