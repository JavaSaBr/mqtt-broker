package javasabr.mqtt.model.database;

import java.time.Duration;

public interface DatabasePoolProperties {

  Duration maxIdleTime();

  int initialPoolSize();

  int maxPoolSize();
}

