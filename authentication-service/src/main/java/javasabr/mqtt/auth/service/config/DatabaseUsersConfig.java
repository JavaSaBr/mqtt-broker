package javasabr.mqtt.auth.service.config;

import java.util.Map;
import javasabr.mqtt.auth.service.config.property.DatabaseCredentials;

public interface DatabaseUsersConfig {

  Map<String, DatabaseCredentials> users();
}

