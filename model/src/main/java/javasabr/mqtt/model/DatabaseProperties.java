package javasabr.mqtt.model;

import java.time.Duration;

public interface DatabaseProperties {
  String driver();

  String host();

  int port();

  String name();

  String credentialsQuery();

  Duration maxIdleTime();

  int initialPoolSize();

  int maxPoolSize();

  String lockTimeout();

  String statementTimeout();
}

