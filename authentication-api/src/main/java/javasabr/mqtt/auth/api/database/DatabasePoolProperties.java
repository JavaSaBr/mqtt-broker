package javasabr.mqtt.auth.api.database;

import java.time.Duration;
import javasabr.mqtt.base.util.PropertyAssert;

public record DatabasePoolProperties(Duration maxIdleTime, int initialSize, int maxSize) {

  public DatabasePoolProperties(int maxIdleTimeSeconds, int initialSize, int maxSize) {
    PropertyAssert.positive(maxIdleTimeSeconds, "Database pool max idle time");
    PropertyAssert.positive(initialSize, "Database pool initial size");
    PropertyAssert.positive(maxSize, "Database pool max size");

    this(Duration.ofSeconds(maxIdleTimeSeconds), initialSize, maxSize);
  }
}
