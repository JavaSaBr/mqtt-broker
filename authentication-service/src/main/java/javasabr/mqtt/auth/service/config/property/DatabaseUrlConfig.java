package javasabr.mqtt.auth.service.config.property;

public interface DatabaseUrlConfig {
  DatabaseDriver dbDriver();

  String dbHost();

  int dbPort();

  String dbName();
}

