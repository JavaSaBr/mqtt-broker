package javasabr.mqtt.model.session;

import java.time.Duration;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.ReasonCode;
import javasabr.mqtt.model.session.exception.AlreadyRegisteredMessageMetaException;
import javasabr.mqtt.model.session.exception.NotFoundMessageMetaException;
import org.jspecify.annotations.Nullable;

public interface MessageTacker {

  @Nullable
  TrackedMessageMeta stored(int messageId);

  /**
   * @throws AlreadyRegisteredMessageMetaException if message meta for the message id is already registered.
   */
  void add(int messageId, MqttMessageType messageType);

  /**
   * @throws AlreadyRegisteredMessageMetaException if message meta for the message id is already registered.
   */
  void add(int messageId, MqttMessageType messageType, @Nullable ReasonCode reasonCode);

  /**
   * @throws AlreadyRegisteredMessageMetaException if message meta for the message id is already registered.
   */
  void add(int messageId, MqttMessageType messageType, @Nullable ReasonCode reasonCode, @Nullable Duration expiration);
  
  /**
   * @return updated version of the meta.
   * @throws NotFoundMessageMetaException if message meta doesn't exist for the message id.
   */
  TrackedMessageMeta update(int messageId, MqttMessageType messageType, @Nullable ReasonCode reasonCode);

  /**
   * @throws NotFoundMessageMetaException if message meta doesn't exist for the message id.
   */
  TrackedMessageMeta remove(int messageId);
  
  @Nullable
  TrackedMessageMeta removeIfExist(int messageId);
}
