package javasabr.mqtt.network.message.out;

import javasabr.mqtt.network.message.HasMessageId;
import javasabr.mqtt.network.message.MqttMessageType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Accessors(fluent = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class PublishMqttOutMessage extends MqttOutMessage implements HasMessageId {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.PUBLISH.ordinal();

  @Getter
  int messageId;

  @Override
  protected byte messageType() {
    return MESSAGE_TYPE;
  }
}
