package javasabr.mqtt.network.packet.out;

import javasabr.mqtt.network.packet.HasPacketId;
import javasabr.mqtt.network.packet.PacketType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class PublishOutPacket extends MqttWritablePacket implements HasPacketId {

  private static final byte PACKET_TYPE = (byte) PacketType.PUBLISH.ordinal();

  @Getter
  protected final int packetId;

  @Override
  protected byte getPacketType() {
    return PACKET_TYPE;
  }
}
