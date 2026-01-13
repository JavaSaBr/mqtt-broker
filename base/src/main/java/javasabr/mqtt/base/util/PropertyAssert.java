package javasabr.mqtt.base.util;

import javasabr.rlib.common.util.StringUtils;

public class PropertyAssert {

  public static void positive(int value, String property) {
    if (value <= 0) {
      throw new BrokerConfigurationException("%s '%s' is not valid".formatted(property, value));
    }
  }

  public static void notEmpty(String value, String property) {
    if (StringUtils.isEmpty(value)) {
      throw new BrokerConfigurationException("%s is not specified".formatted(property));
    }
  }

  public static void notNull(Object value, String message, Object... args) {
    if (value == null) {
      throw new BrokerConfigurationException(message.formatted(args));
    }
  }
}
