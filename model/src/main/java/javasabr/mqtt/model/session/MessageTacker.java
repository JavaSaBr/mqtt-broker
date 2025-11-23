package javasabr.mqtt.model.session;

import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.ReasonCode;
import org.jspecify.annotations.Nullable;

public interface MessageTacker {

  @Nullable
  TrackedMessageMeta stored(int messageId);

  void add(int messageId, MqttMessageType messageType);

  void add(int messageId, MqttMessageType messageType, @Nullable ReasonCode reasonCode);

  /**
   * @return true if was added a new entry instead of updating current
   */
  boolean update(int messageId, MqttMessageType messageType, @Nullable ReasonCode reasonCode);

  @Nullable
  TrackedMessageMeta remove(int messageId);
}
