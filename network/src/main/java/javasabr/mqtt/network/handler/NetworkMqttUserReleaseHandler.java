package javasabr.mqtt.network.handler;

import javasabr.mqtt.network.user.ConfigurableNetworkMqttUser;
import reactor.core.publisher.Mono;

public interface NetworkMqttUserReleaseHandler {

  Mono<?> release(ConfigurableNetworkMqttUser user);
}
