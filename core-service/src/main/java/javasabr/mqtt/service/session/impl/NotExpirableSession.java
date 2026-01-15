package javasabr.mqtt.service.session.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class NotExpirableSession {
  long storedAt;
  InMemoryNetworkMqttSession session;

  static NotExpirableSession of(InMemoryNetworkMqttSession session) {
    return new NotExpirableSession(System.currentTimeMillis(), session);
  }
}
