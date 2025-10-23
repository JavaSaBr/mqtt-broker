package javasabr.mqtt.service;

import javasabr.mqtt.network.MqttConnection;

public interface ConnectionService {

  void processAcceptedConnection(MqttConnection connection);
}
