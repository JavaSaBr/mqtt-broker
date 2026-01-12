package javasabr.mqtt.auth.api.database;

import java.time.Duration;

public record DatabasePoolProperties(Duration maxIdleTime, int initialSize, int maxSize) {

  public DatabasePoolProperties(int maxIdleTimeSeconds, int initialSize, int maxSize) {
    PropertyAssert.positive(maxIdleTimeSeconds, "Database pool max idle time '%s' is not valid");
    PropertyAssert.positive(initialSize, "Database pool initial size '%s' is not valid");
    PropertyAssert.positive(maxSize, "Database pool max size '%s' is not valid");

    this(Duration.ofSeconds(maxIdleTimeSeconds), initialSize, maxSize);
  }
}
