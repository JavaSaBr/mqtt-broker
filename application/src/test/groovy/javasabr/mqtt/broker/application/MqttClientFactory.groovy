package javasabr.mqtt.broker.application

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient

import java.util.concurrent.atomic.AtomicInteger

class MqttClientFactory {

  private static final ID_GENERATOR = new AtomicInteger(1)

  static Mqtt5AsyncClient buildMqtt5Client(String clientId, InetSocketAddress address) {
    return MqttClient.builder()
        .identifier(clientId)
        .serverHost(address.getHostName())
        .serverPort(address.getPort())
        .useMqttVersion5()
        .addDisconnectedListener {
          println "[${clientId}|mqtt5] disconnected:[${it.cause?.message}]"
        }
        .build()
        .toAsync()
  }

  static Mqtt3AsyncClient buildMqtt311Client(String clientId, InetSocketAddress address) {
    return MqttClient.builder()
        .identifier(clientId)
        .serverHost(address.getHostName())
        .serverPort(address.getPort())
        .useMqttVersion3()
        .addDisconnectedListener {
          println "[${clientId}|mqtt311] disconnected:[${it.cause?.message}]"
        }
        .build()
        .toAsync()
  }

  static String generateClientId(String prefix) {
    return prefix + "_" + ID_GENERATOR.incrementAndGet()
  }
}
