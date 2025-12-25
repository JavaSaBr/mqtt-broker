package javasabr.mqtt.auth.service.config;

import javasabr.mqtt.auth.service.config.property.DatabaseUrlConfig;

public interface DatabaseUrlBuilder {
  String build(DatabaseUrlConfig databaseUrlConfig);
}
