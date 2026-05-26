package javasabr.mqtt.broker.application.config;

import java.net.InetSocketAddress;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttConnectionFactory;
import javasabr.mqtt.network.message.MqttPacketCodec;
import javasabr.mqtt.network.user.NetworkMqttUserFactory;
import javasabr.mqtt.service.ConnectionService;
import javasabr.mqtt.service.impl.PlainMqttConnectionFactory;
import javasabr.rlib.network.NetworkFactory;
import javasabr.rlib.network.ServerNetworkConfig;
import javasabr.rlib.network.server.ServerNetwork;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@CustomLog
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "mqtt.external.plain.network.enabled", matchIfMissing = true)
public class MqttExternalPlainNetworkConfig {

  @Bean
  ServerNetworkConfig externalNetworkConfig(
      @Value("${mqtt.external.network.read.buffer.size:512}") int readBufferSize,
      @Value("${mqtt.external.network.pending.buffer.size:1024}") int pendingBufferSize,
      @Value("${mqtt.external.network.write.buffer.size:512}") int writeBufferSize,
      @Value("${mqtt.external.network.thread.group.name:ExternalNetwork}") String threadGroupName,
      @Value("${mqtt.external.network.thread.count:1}") int threadGroupMaxSize) {
    return ServerNetworkConfig.SimpleServerNetworkConfig
        .builder()
        .readBufferSize(readBufferSize)
        .pendingBufferSize(pendingBufferSize)
        .writeBufferSize(writeBufferSize)
        .threadGroupName(threadGroupName)
        .threadGroupMaxSize(threadGroupMaxSize)
        .build();
  }

  @Bean
  MqttConnectionFactory externalConnectionFactory(
      MqttServerConnectionConfig externalServerConnectionConfig,
      NetworkMqttUserFactory mqttUserFactory,
      @Value("${mqtt.external.connection.max.packets.by.read:100}") int maxPacketsByRead,
      MqttPacketCodec mqttPacketCodec) {
    return new PlainMqttConnectionFactory(
        externalServerConnectionConfig,
        mqttUserFactory,
        maxPacketsByRead, mqttPacketCodec);
  }

  @Bean
  InetSocketAddress externalNetworkAddress(
      @Value("${mqtt.external.network.host:localhost}") String host,
      @Value("${mqtt.external.network.port:1883}") int port) {
    return new InetSocketAddress(host, port);
  }

  @Bean
  ServerNetwork<MqttConnection> externalNetwork(
      ServerNetworkConfig externalNetworkConfig,
      MqttConnectionFactory externalConnectionFactory) {
    return NetworkFactory.serverNetwork(externalNetworkConfig, externalConnectionFactory::newConnection);
  }

  @Bean
  ApplicationListener<ApplicationStartedEvent> externalNetworkStarter(
      ServerNetwork<MqttConnection> externalNetwork,
      ConnectionService connectionService,
      InetSocketAddress externalNetworkAddress) {
    return _ -> {
      externalNetwork.start(externalNetworkAddress);
      externalNetwork.onAccept(connectionService::processAcceptedConnection);
      log.info(externalNetworkAddress, "Started external MQTT network by address:[%s]"::formatted);
    };
  }
}
