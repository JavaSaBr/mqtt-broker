package javasabr.mqtt.legacy.network.packet.out;

import javasabr.mqtt.legacy.model.MqttVersion;
import javasabr.mqtt.legacy.model.QoS;
import javasabr.mqtt.legacy.network.packet.PacketType;
import java.nio.ByteBuffer;
import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

/**
 * Connect request.
 */
@RequiredArgsConstructor
public class Connect311OutPacket extends MqttWritablePacket {

  private static final byte PACKET_TYPE = (byte) PacketType.CONNECT.ordinal();

  private final String username;
  private final String willTopic;
  private final String clientId;

  private final byte[] password;
  private final byte[] willPayload;

  private final QoS willQos;

  private final int keepAlive;

  private final boolean willRetain;
  private final boolean cleanStart;

  public Connect311OutPacket(String clientId, int keepAlive) {
    this(
        StringUtils.EMPTY,
        StringUtils.EMPTY,
        clientId,
        ArrayUtils.EMPTY_BYTE_ARRAY,
        ArrayUtils.EMPTY_BYTE_ARRAY,
        QoS.AT_MOST_ONCE,
        keepAlive,
        false,
        false);
  }

  protected MqttVersion getMqttVersion() {
    return MqttVersion.MQTT_3_1_1;
  }

  @Override
  protected byte getPacketType() {
    return PACKET_TYPE;
  }

  @Override
  protected void writeVariableHeader(ByteBuffer buffer) {

    var mqttVersion = getMqttVersion();

    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718030
    writeString(buffer, mqttVersion.getName());
    writeByte(buffer, mqttVersion.getVersion());
    writeByte(buffer, buildConnectFlags());
    writeShort(buffer, keepAlive);
  }

  @Override
  protected void writePayload(ByteBuffer buffer) {

    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718031
    writeString(buffer, clientId);

    if (StringUtils.isNotEmpty(willTopic)) {
      appendWillProperties(buffer);
      writeString(buffer, willTopic);
      writeBytes(buffer, willPayload);
    }

    if (StringUtils.isNotEmpty(username)) {
      writeString(buffer, username);
    }

    if (ArrayUtils.isNotEmpty(password)) {
      writeBytes(buffer, password);
    }
  }

  private int buildConnectFlags() {

    int connectFlags = 0;

    if (StringUtils.isNotEmpty(username)) {
      connectFlags |= 0b1000_0000;
    }

    if (ArrayUtils.isNotEmpty(password)) {
      connectFlags |= 0b0100_0000;
    }

    if (StringUtils.isNotEmpty(willTopic)) {
      connectFlags |= 0b0000_0100;
      connectFlags |= (willQos.ordinal() << 3);
      if (willRetain) {
        connectFlags |= 0b0010_0000;
      }
    }

    if (cleanStart) {
      connectFlags |= 0b0000_0010;
    }

    return connectFlags;
  }

  protected void appendWillProperties(ByteBuffer buffer) {
  }
}
