package javasabr.mqtt.auth.api.database;

import java.time.Duration;

public interface DatabasePoolProperties {

  Duration maxIdleTime();

  int initialSize();

  int maxSize();
}

