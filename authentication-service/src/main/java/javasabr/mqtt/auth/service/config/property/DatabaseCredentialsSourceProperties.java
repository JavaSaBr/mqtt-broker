package javasabr.mqtt.auth.service.config.property;

import javasabr.mqtt.auth.service.config.DatabaseUsersConfig;

public interface DatabaseCredentialsSourceProperties extends SwitchableProperty, DatabasePoolConfig, DatabaseUrlConfig,
    DatabaseUsersConfig, DatabaseTimeoutsConfig {

  DatabaseDriver dbDriver();
  String dbHost();
  int dbPort();
  String dbName();
}
