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
public enum CredentialsSourceType {
  FILE(1),
  REDIS(2),
  DATABASE(3),
  LDAP(4),
  HTTP(5),
  SYSTEM(6);

  int priority;
}
