package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.network.MqttConnection;
import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Connect request.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class ConnectMqtt311OutMessage extends MqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.CONNECT.ordinal();

  String username;
  String willTopic;
  String clientId;

  byte[] password;
  byte[] willPayload;

  QoS willQos;

  int keepAlive;

  boolean willRetain;
  boolean cleanStart;

  public ConnectMqtt311OutMessage(String clientId, int keepAlive) {
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

  protected MqttVersion mqttVersion() {
    return MqttVersion.MQTT_3_1_1;
  }

  @Override
  protected byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    MqttVersion mqttVersion = mqttVersion();
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718030
    writeString(buffer, mqttVersion.rawName());
    writeByte(buffer, mqttVersion.version());
    writeByte(buffer, buildConnectFlags());
    writeShort(buffer, keepAlive);
  }

  @Override
  protected void writePayload(MqttConnection connection, ByteBuffer buffer) {

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

  protected void appendWillProperties(ByteBuffer buffer) {}
}
