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
public enum AuthenticationType {
  X509(1),
  IP_CIDR(2),
  ENHANCED(3),
  JWT(4),
  OAUTH(5),
  BASIC(6),
  LDAP(7),
  ANONYMOUS(8);

  int priority;
}
