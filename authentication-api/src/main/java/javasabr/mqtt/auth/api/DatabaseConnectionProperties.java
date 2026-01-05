package javasabr.mqtt.auth.api;

public interface DatabaseConnectionProperties {
  DatabaseDriver driver();

  String host();

  int port();

  String name();
}

