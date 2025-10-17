package javasabr.mqtt.network.packet.in;

import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.PacketProperty;
import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.packet.HasPacketId;
import javasabr.mqtt.network.packet.PacketType;
import javasabr.mqtt.base.utils.DebugUtils;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Publish release (QoS 2 delivery part 2).
 */
@Getter
@Accessors(fluent = true, chain = false)
public class PublishReleaseInPacket extends MqttReadablePacket implements HasPacketId<MqttConnection> {

  private static final byte PACKET_TYPE = (byte) PacketType.PUBLISH_RELEASED.ordinal();

  static {
    DebugUtils.registerIncludedFields("reasonCode", "packetId");
  }

  private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
        /*
          Followed by the UTF-8 Encoded String representing the reason associated with this response. This
          Reason String is human readable, designed for diagnostics and SHOULD NOT be parsed by the
          receiver.

          The sender uses this value to give additional information to the receiver. The sender MUST NOT send
          this Property if it would increase the size of the PUBREL packet beyond the Maximum Packet Size
          specified by the receiver [MQTT-3.6.2-2]. It is a Protocol Error to include the Reason String more than
          once.
         */
      PacketProperty.REASON_STRING,
        /*
          Followed by UTF-8 String Pair. This property can be used to provide additional diagnostic or other
          information for the PUBREL. The sender MUST NOT send this property if it would increase the size of the
          PUBREL packet beyond the Maximum Packet Size specified by the receiver [MQTT-3.6.2-3]. The User
          Property is allowed to appear multiple times to represent multiple name, value pairs. The same name is
          allowed to appear more than once
         */
      PacketProperty.USER_PROPERTY);

  private PublishReleaseReasonCode reasonCode;
  private int packetId;

  // properties
  private String reason;

  public PublishReleaseInPacket(byte info) {
    super(info);
    this.reasonCode = PublishReleaseReasonCode.SUCCESS;
    this.reason = "";
  }

  @Override
  public byte packetType() {
    return PACKET_TYPE;
  }

  @Override
  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    super.readVariableHeader(connection, buffer);

    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718055
    packetId = readUnsignedShort(buffer);

    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901143
    if (connection.isSupported(MqttVersion.MQTT_5) && buffer.hasRemaining()) {
      reasonCode = PublishReleaseReasonCode.of(readUnsignedByte(buffer));
    }
  }

  @Override
  protected boolean isPropertiesSupported(MqttConnection connection, ByteBuffer buffer) {
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901143
    return super.isPropertiesSupported(connection, buffer) && buffer.hasRemaining();
  }

  @Override
  protected Set<PacketProperty> getAvailableProperties() {
    return AVAILABLE_PROPERTIES;
  }

  @Override
  protected void applyProperty(PacketProperty property, String value) {
    switch (property) {
      case REASON_STRING:
        reason = value;
        break;
      default:
        unexpectedProperty(property);
    }
  }
}
