package javasabr.mqtt.network;

import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;

public interface MqttClientFactory {

  UnsafeMqttClient newClient(MqttConnection connection);
}
