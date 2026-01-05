package javasabr.mqtt.auth.api;

public interface DatabaseConnectionProperties {
  DatabaseDriver dbDriver();

  String dbHost();

  int dbPort();

  String dbName();
}

