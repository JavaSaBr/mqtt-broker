package javasabr.mqtt.network.packet.out;

import javasabr.mqtt.network.packet.HasPacketId;
import javasabr.mqtt.network.packet.PacketType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@RequiredArgsConstructor
public abstract class PublishOutPacket extends MqttWritablePacket implements HasPacketId {

  private static final byte PACKET_TYPE = (byte) PacketType.PUBLISH.ordinal();

  @Getter
  protected final int packetId;

  @Override
  protected byte packetType() {
    return PACKET_TYPE;
  }
}
