package javasabr.mqtt.service.session.impl;

import com.fasterxml.jackson.annotation.JsonValue;
import java.time.Duration;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class ExpirableSession extends NotExpirableSession {
  long expireAfter;

  static ExpirableSession of(Duration expiryInterval, InMemoryNetworkMqttSession session) {
    long currentTime = System.currentTimeMillis();
    long expireAfter = currentTime + expiryInterval.toMillis();
    return new ExpirableSession(currentTime, session, expireAfter);
  }

  ExpirableSession(long storedAt, InMemoryNetworkMqttSession session, long expireAfter) {
    super(storedAt, session);
    this.expireAfter = expireAfter;
  }

  @Override
  @JsonValue
  public Map<?, ?> jsonDebugValue() {
    return Map.of(
        "storedAt", storedAt(),
        "expireAfter", expireAfter,
        "clientId", session().clientId());
  }
}
