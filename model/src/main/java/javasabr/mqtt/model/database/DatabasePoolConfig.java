package javasabr.mqtt.model.database;

import java.time.Duration;

public interface DatabasePoolConfig {

  Duration maxIdleTime();

  int initialPoolSize();

  int maxPoolSize();
}

