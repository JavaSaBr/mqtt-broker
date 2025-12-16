package javasabr.mqtt.service.auth.source;

import java.time.Duration;

public interface DatabaseProperties {
  String username();
  String password();
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
