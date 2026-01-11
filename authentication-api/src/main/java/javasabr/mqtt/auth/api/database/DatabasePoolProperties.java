package javasabr.mqtt.auth.api.database;

import java.time.Duration;
import javasabr.mqtt.auth.api.exception.AuthenticationConfigException;

public record DatabasePoolProperties(Duration maxIdleTime, int initialSize, int maxSize) {
  public DatabasePoolProperties(int maxIdleTimeSeconds, int initialSize, int maxSize) {
    assertPositive(maxIdleTimeSeconds, "Database pool max idle time '%s' is not valid");
    assertPositive(initialSize, "Database pool initial size '%s' is not valid");
    assertPositive(maxSize, "Database pool max size '%s' is not valid");

    this(Duration.ofSeconds(maxIdleTimeSeconds), initialSize, maxSize);
  }

  public static void assertPositive(int value, String message) {
    if (value <= 0) {
      throw new AuthenticationConfigException(message.formatted(value));
    }
  }
}
