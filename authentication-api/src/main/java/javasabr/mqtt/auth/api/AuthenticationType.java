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
  ENHANCED(2),
  JWT(3),
  BASIC(4),
  ANONYMOUS(5);

  int priority;
}
