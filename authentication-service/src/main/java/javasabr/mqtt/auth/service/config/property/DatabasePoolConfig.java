package javasabr.mqtt.auth.service.config.property;

import java.time.Duration;

public interface DatabasePoolConfig {

  Duration maxIdleTime();

  int initialPoolSize();

  int maxPoolSize();
}

