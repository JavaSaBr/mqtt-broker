package javasabr.mqtt.service.session.impl;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import javasabr.mqtt.base.util.DebugUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotExpirableSession {
  long storedAt;
  InMemoryNetworkMqttSession wrapped;

  static NotExpirableSession of(InMemoryNetworkMqttSession session) {
    return new NotExpirableSession(System.currentTimeMillis(), session);
  }

  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }

  @JsonValue
  public Map<?, ?> jsonDebugValue() {
    return Map.of("storedAt", storedAt, "clientId", wrapped.clientId());
  }
}
