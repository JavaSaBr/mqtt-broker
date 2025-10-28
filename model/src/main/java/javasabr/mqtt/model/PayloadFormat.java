package javasabr.mqtt.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum PayloadFormat {
  BINARY(0),
  UTF8_STRING(1),
  INVALID(2),
  UNDEFINED(3);

  public static PayloadFormat fromCode(long code) {
    if (BINARY.code == code) {
      return BINARY;
    } else if (UTF8_STRING.code == code) {
      return UTF8_STRING;
    }
    return UNDEFINED;
  }

  int code;
}
