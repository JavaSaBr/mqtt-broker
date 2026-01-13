package javasabr.mqtt.base.util;

import javasabr.rlib.common.util.StringUtils;

public class PropertyAssert {

  public static void positive(int value, String message) {
    if (value <= 0) {
      throw new BrokerConfigurationException(message.formatted(value));
    }
  }

  public static void notEmpty(String value, String message) {
    if (StringUtils.isEmpty(value)) {
      throw new BrokerConfigurationException(message);
    }
  }

  public static void notNull(Object value, String message) {
    if (value == null) {
      throw new BrokerConfigurationException(message);
    }
  }
}
