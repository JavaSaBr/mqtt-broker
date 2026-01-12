package javasabr.mqtt.auth.api.database;

import javasabr.mqtt.auth.api.exception.AuthenticationConfigException;
import javasabr.rlib.common.util.StringUtils;

public class PropertyAssert {

  public static void positive(int value, String message) {
    if (value <= 0) {
      throw new AuthenticationConfigException(message.formatted(value));
    }
  }

  public static void notEmpty(String value, String message) {
    if (StringUtils.isEmpty(value)) {
      throw new AuthenticationConfigException(message);
    }
  }

  public static void notNull(Object value, String message) {
    if (value == null) {
      throw new AuthenticationConfigException(message);
    }
  }
}
