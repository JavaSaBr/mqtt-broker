package javasabr.mqtt.model.session;

import java.time.Duration;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.ReasonCode;
import org.jspecify.annotations.Nullable;

public interface MessageTacker {

  @Nullable
  TrackedMessageMeta stored(int messageId);

  void add(int messageId, MqttMessageType messageType);

  void add(int messageId, MqttMessageType messageType, @Nullable ReasonCode reasonCode);

  void add(
      int messageId,
      MqttMessageType messageType, 
      @Nullable ReasonCode reasonCode,
      @Nullable Duration expiration);
  
  /**
   * @return updated version of the meta.
   */
  TrackedMessageMeta update(int messageId, MqttMessageType messageType, @Nullable ReasonCode reasonCode);

  @Nullable
  TrackedMessageMeta remove(int messageId);
}
