package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.publish.PublishData;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.MqttConnection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class PublishMqtt311OutMessage extends TrackableMqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.PUBLISH.ordinal();

  static {
    DebugUtils.registerIncludedFields("qos", "topicName", "duplicate");
  }

  QoS qos;
  PublishData data;
  TopicName topicName;

  boolean retain;
  boolean duplicate;

  public PublishMqtt311OutMessage(
      int messageId,
      QoS qos,
      boolean retain,
      boolean duplicate,
      TopicName topicName,
      PublishData data) {
    super(messageId);
    this.qos = qos;
    this.retain = retain;
    this.duplicate = duplicate;
    this.data = data;
    this.topicName = topicName;
  }

  @Override
  protected byte messageTypeId() {
    return MESSAGE_TYPE;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH;
  }

  @Override
  public int expectedLength(MqttConnection connection) {
    return 7 + data.payloadSize();
  }

  @Override
  protected byte messageFlags() {
    byte info = (byte) (qos.ordinal() << 1);
    if (retain) {
      info |= 0b0001;
    }
    if (duplicate) {
      info |= 0b1000;
    }
    return info;
  }

  @Override
  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc384800412
    writeString(buffer, topicName.rawTopic());
    if (qos.isHigherThan(QoS.AT_MOST_ONCE)) {
      writeShort(buffer, messageId);
    }
  }

  @Override
  protected void writePayload(MqttConnection connection, ByteBuffer buffer) {
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc384800413
    data.writePayloadTo(buffer);
  }
}
