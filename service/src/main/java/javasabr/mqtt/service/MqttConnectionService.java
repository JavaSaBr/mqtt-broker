package javasabr.mqtt.service;

import javasabr.mqtt.network.MqttConnection;

public interface MqttConnectionService {

  void processAcceptedConnection(MqttConnection connection);
}
