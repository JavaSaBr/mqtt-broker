package javasabr.mqtt.auth.api.database;

public interface DatabaseConnectionProperties {
  DatabaseDriver driver();

  String host();

  int port();

  String name();
}

