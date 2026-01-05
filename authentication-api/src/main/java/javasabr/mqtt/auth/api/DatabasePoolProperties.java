package javasabr.mqtt.auth.api;

import java.time.Duration;

public interface DatabasePoolProperties {

  Duration maxIdleTime();

  int initialSize();

  int maxSize();
}

