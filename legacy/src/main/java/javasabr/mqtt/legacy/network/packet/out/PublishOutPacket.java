package javasabr.mqtt.legacy.network.packet.out;

import javasabr.mqtt.legacy.network.packet.HasPacketId;
import javasabr.mqtt.legacy.network.packet.PacketType;
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
