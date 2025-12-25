package javasabr.mqtt.auth.service.config;

import java.util.Map;
import javasabr.mqtt.auth.service.config.property.Credentials;

public interface DatabaseUsersConfig {

  Map<String, Credentials> users();
}

