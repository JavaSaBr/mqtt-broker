package javasabr.mqtt.network.user;

import javasabr.mqtt.network.MqttConnection;

public interface NetworkMqttUserFactory {

  ConfigurableNetworkMqttUser createNetworkUser(MqttConnection connection);
}
