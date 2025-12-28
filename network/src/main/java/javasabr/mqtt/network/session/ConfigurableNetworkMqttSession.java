package javasabr.mqtt.network.session;

import java.time.Duration;
import org.jspecify.annotations.Nullable;

public interface ConfigurableNetworkMqttSession extends NetworkMqttSession {
 
  void expiryInterval(@Nullable Duration expiryInterval);
}
