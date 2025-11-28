package javasabr.mqtt.network.user;

import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttNetworkSession;
import javasabr.mqtt.network.message.out.ConnectAckMqtt311OutMessage;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

public interface ConfigurableNetworkMqttUser extends NetworkMqttUser {

  MqttConnection connection();

  void clientId(String clientId);

  void session(@Nullable MqttNetworkSession session);

  void reject(ConnectAckMqtt311OutMessage connectAsk);

  Mono<?> release();
}
