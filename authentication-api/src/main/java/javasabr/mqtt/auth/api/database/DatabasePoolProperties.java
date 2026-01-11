package javasabr.mqtt.auth.api.database;

import java.time.Duration;
import javasabr.mqtt.auth.api.exception.AuthenticationConfigException;

public record DatabasePoolProperties(Duration maxIdleTime, int initialSize, int maxSize) {
  public DatabasePoolProperties(int maxIdleTimeSeconds, int initialSize, int maxSize) {
    if (maxIdleTimeSeconds <= 0) {
      throw new AuthenticationConfigException("Database pool max idle time '%s' is not valid".formatted(
          maxIdleTimeSeconds));
    }
    if (initialSize <= 0) {
      throw new AuthenticationConfigException("Database pool initial size '%s' is not valid".formatted(initialSize));
    }
    if (maxSize <= 0) {
      throw new AuthenticationConfigException("Database pool max size '%s' is not valid".formatted(maxSize));
    }
    this(Duration.ofSeconds(maxIdleTimeSeconds), initialSize, maxSize);
  }
}
