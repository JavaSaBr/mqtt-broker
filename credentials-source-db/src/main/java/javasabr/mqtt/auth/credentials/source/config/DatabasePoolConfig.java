package javasabr.mqtt.auth.credentials.source.config;

import java.time.Duration;

public interface DatabasePoolConfig {

  Duration maxIdleTime();

  int initialPoolSize();

  int maxPoolSize();
}

