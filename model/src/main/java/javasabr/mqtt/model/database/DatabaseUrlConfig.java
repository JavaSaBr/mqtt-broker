package javasabr.mqtt.model.database;

public interface DatabaseUrlConfig {
  String driver();

  String host();

  int port();

  String name();
}

