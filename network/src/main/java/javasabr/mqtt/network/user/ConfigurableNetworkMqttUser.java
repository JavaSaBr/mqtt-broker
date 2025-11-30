package javasabr.mqtt.network.user;

import javasabr.mqtt.network.message.out.ConnectAckMqtt311OutMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

public interface ConfigurableNetworkMqttUser extends NetworkMqttUser {
  
  void clientId(String clientId);

  void session(@Nullable NetworkMqttSession session);

  void reject(ConnectAckMqtt311OutMessage connectAsk);

  Mono<?> release();
}
