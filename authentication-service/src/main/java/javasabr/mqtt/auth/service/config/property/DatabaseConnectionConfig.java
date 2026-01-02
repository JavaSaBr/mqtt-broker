package javasabr.mqtt.auth.service.config.property;

public interface DatabaseConnectionConfig {
  DatabaseDriver dbDriver();

  String dbHost();

  int dbPort();

  String dbName();
}

