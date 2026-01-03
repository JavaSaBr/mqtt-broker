package javasabr.mqtt.auth.service.config.property;

import java.time.Duration;

public interface DatabasePoolProperties {

  Duration maxIdleTime();

  int initialPoolSize();

  int maxPoolSize();
}

