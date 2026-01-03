package javasabr.mqtt.auth.service.config.property;

public interface DatabaseConnectionProperties {
  DatabaseDriver dbDriver();

  String dbHost();

  int dbPort();

  String dbName();
}

