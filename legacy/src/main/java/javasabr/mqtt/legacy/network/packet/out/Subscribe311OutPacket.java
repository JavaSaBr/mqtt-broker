package javasabr.mqtt.legacy.network.packet.out;

import javasabr.mqtt.legacy.model.SubscribeTopicFilter;
import javasabr.mqtt.legacy.network.packet.PacketType;
import java.nio.ByteBuffer;
import javasabr.rlib.collections.array.Array;
import lombok.RequiredArgsConstructor;

/**
 * Subscribe request.
 */
@RequiredArgsConstructor
public class Subscribe311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) PacketType.SUBSCRIBE.ordinal();

  private final Array<SubscribeTopicFilter> topicFilters;
  private final int packetId;

  @Override
  protected byte getPacketType() {
    return PACKET_TYPE;
  }

  @Override
  protected void writeVariableHeader(ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718065
    writeShort(buffer, packetId);
  }

  @Override
  protected void writePayload(ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718066
    for (var topicFilter : topicFilters) {
      writeString(buffer,
          topicFilter
              .getTopicFilter()
              .toString());
      writeByte(buffer, buildSubscriptionOptions(topicFilter));
    }
  }

  protected int buildSubscriptionOptions(SubscribeTopicFilter topicFilter) {
    return topicFilter
        .getQos()
        .ordinal();
  }
}
