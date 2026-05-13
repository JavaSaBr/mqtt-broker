package javasabr.mqtt.model.session;

import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.ReasonCode;
import org.jspecify.annotations.Nullable;

public interface TrackedMessageMeta {

  MqttMessageType messageType();

  @Nullable
  ReasonCode reasonCode();
  
  long expiredAt();
}
