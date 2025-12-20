package javasabr.mqtt.model;

import javasabr.mqtt.model.database.DatabaseUrlProperties;

public interface DatabaseUrlBuilder {
  String build(DatabaseUrlProperties databaseProperties);
}
