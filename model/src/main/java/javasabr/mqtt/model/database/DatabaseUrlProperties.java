package javasabr.mqtt.model.database;

import java.time.Duration;

public interface DatabaseUrlProperties {
  String driver();

  String host();

  int port();

  String name();
}

