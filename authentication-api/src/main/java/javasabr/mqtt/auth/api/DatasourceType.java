package javasabr.mqtt.auth.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum DatasourceType {
  MEMORY(1),
  REDIS(2),
  DATABASE(3),
  FILE(4),
  LDAP(5),
  HTTP(6),
  SYSTEM(7);

  int priority;
}
