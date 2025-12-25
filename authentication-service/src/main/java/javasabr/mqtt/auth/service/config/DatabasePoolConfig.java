package javasabr.mqtt.auth.service.config;

import java.time.Duration;

public interface DatabasePoolConfig {

  Duration maxIdleTime();

  int initialPoolSize();

  int maxPoolSize();
}

