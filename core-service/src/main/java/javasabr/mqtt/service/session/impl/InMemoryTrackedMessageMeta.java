package javasabr.mqtt.service.session.impl;

import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.ReasonCode;
import javasabr.mqtt.model.session.TrackedMessageMeta;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@Accessors
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InMemoryTrackedMessageMeta implements TrackedMessageMeta {

  static {
    DebugUtils.registerIncludedFields("messageType", "reasonCode");
  }
  
  MqttMessageType messageType;
  @Nullable
  ReasonCode reasonCode;

  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }
}
