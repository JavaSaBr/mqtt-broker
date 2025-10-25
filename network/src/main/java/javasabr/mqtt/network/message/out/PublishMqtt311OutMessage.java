package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import javasabr.mqtt.base.utils.DebugUtils;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.network.MqttConnection;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class PublishMqtt311OutMessage extends PublishMqttOutMessage {

  static {
    DebugUtils.registerIncludedFields("qos", "topicName", "duplicate");
  }

  QoS qos;
  byte[] payload;
  String topicName;

  boolean retained;
  boolean duplicate;

  public PublishMqtt311OutMessage(
      int messageId,
      QoS qos,
      boolean retained,
      boolean duplicate,
      String topicName,
      byte[] payload) {
    super(messageId);
    this.qos = qos;
    this.retained = retained;
    this.duplicate = duplicate;
    this.payload = payload;
    this.topicName = topicName;
  }

  @Override
  public int expectedLength(MqttConnection connection) {
    return 7 + payload.length;
  }

  @Override
  protected byte packetFlags() {

    byte info = (byte) (qos.ordinal() << 1);

    if (retained) {
      info |= 0x01;
    }

    if (duplicate) {
      info |= 0x08;
    }

    return info;
  }

  @Override
  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc384800412
    writeString(buffer, topicName);
    if (qos.ordinal() > QoS.AT_MOST_ONCE.ordinal()) {
      writeShort(buffer, messageId);
    }
  }

  @Override
  protected void writePayload(MqttConnection connection, ByteBuffer buffer) {
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc384800413
    buffer.put(payload);
  }
}
