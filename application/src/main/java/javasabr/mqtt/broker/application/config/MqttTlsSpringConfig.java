package javasabr.mqtt.broker.application.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttConnectionFactory;
import javasabr.mqtt.network.MqttTlsProperties;
import javasabr.mqtt.network.user.NetworkMqttUserFactory;
import javasabr.mqtt.service.ConnectionService;
import javasabr.mqtt.service.impl.MqttSslConnectionFactory;
import javasabr.rlib.network.NetworkFactory;
import javasabr.rlib.network.ServerNetworkConfig;
import javasabr.rlib.network.impl.DefaultBufferAllocator;
import javasabr.rlib.network.server.ServerNetwork;
import javasabr.rlib.network.util.NetworkUtils;
import javax.net.ssl.SSLContext;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@CustomLog
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "mqtt.tls.enabled", havingValue = "true")
public class MqttTlsSpringConfig {

  @Bean
  MqttTlsProperties mqttTlsProperties(
      @Value("${mqtt.tls.keystore-path:}") String keystorePath,
      @Value("${mqtt.tls.keystore-password:}") String keystorePassword,
      @Value("${mqtt.tls.keystore-type:PKCS12}") String keystoreType,
      @Value("${mqtt.tls.truststore-path:}") String truststorePath,
      @Value("${mqtt.tls.truststore-password:}") String truststorePassword,
      @Value("${mqtt.tls.truststore-type:PKCS12}") String truststoreType,
      @Value("${mqtt.tls.require-client-cert:true}") boolean requireClientCert,
      @Value("${mqtt.tls.tls-protocols:#{{'TLSv1.3'}}}") List<String> tlsProtocols,
      @Value("${mqtt.tls.cipher-suites:#{{}}}") List<String> cipherSuites) {
    return MqttTlsProperties.builder()
        .requireClientCert(requireClientCert)
        .keystorePath(keystorePath)
        .keystorePassword(keystorePassword)
        .keystoreType(keystoreType)
        .truststorePassword(truststorePassword)
        .truststorePath(truststorePath)
        .truststoreType(truststoreType)
        .tlsProtocols(tlsProtocols)
        .cipherSuites(cipherSuites)
        .build();
  }

  @Bean
  SSLContext sslContext(MqttTlsProperties properties) throws IOException {
    String keyStoreType = properties.keystoreType();
    String keyStorePath = properties.keystorePath();
    String keyStorePassword = properties.keystorePassword();
    String trustStoreType = properties.truststoreType();
    String trustStorePath = properties.truststorePath();
    String trustStorePassword = properties.truststorePassword();

    try (FileInputStream keyStoreData = new FileInputStream(keyStorePath);
         FileInputStream trustStoreData = trustStorePath.isEmpty() ? null : new FileInputStream(trustStorePath)) {
      return NetworkUtils.createSslContext(
          keyStoreType,
          keyStoreData,
          keyStorePassword,
          trustStoreType,
          trustStoreData,
          trustStorePassword);
    }
  }

  @Bean
  MqttConnectionFactory<MqttConnection> tlsMqttConnectionFactory(
      MqttServerConnectionConfig externalServerConnectionConfig,
      NetworkMqttUserFactory mqttUserFactory,
      @Value("${mqtt.external.connection.max.packets.by.read:100}") int maxPacketsByRead,
      SSLContext sslContext,
      MqttTlsProperties tlsProperties,
      ServerNetworkConfig tlsNetworkConfig) {
    return new MqttSslConnectionFactory(
        externalServerConnectionConfig,
        mqttUserFactory,
        maxPacketsByRead,
        sslContext,
        tlsProperties,
        new DefaultBufferAllocator(tlsNetworkConfig));
  }

  @Bean
  ServerNetworkConfig tlsNetworkConfig(
      @Value("${mqtt.tls.network.read.buffer.size:512}") int readBufferSize,
      @Value("${mqtt.tls.network.pending.buffer.size:1024}") int pendingBufferSize,
      @Value("${mqtt.tls.network.write.buffer.size:1024}") int writeBufferSize,
      @Value("${mqtt.tls.network.thread.group.name:TlsNetwork}") String threadGroupName,
      @Value("${mqtt.tls.network.thread.count:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}") int threadGroupMaxSize) {
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
  InetSocketAddress tlsNetworkAddress(
      @Value("${mqtt.external.network.host:localhost}") String host,
      @Value("${mqtt.tls.network.port:8883}") int port) {
    return new InetSocketAddress(host, port);
  }

  @Bean
  ServerNetwork<MqttConnection> tlsNetwork(
      ServerNetworkConfig tlsNetworkConfig,
      MqttConnectionFactory<MqttConnection> tlsMqttConnectionFactory) {
    return NetworkFactory.serverNetwork(tlsNetworkConfig, tlsMqttConnectionFactory::newConnection);
  }

  @Bean
  ApplicationListener<ApplicationStartedEvent> tlsNetworkStarter(
      ServerNetwork<MqttConnection> tlsNetwork,
      @Qualifier("externalMqttConnectionService") ConnectionService connectionService,
      InetSocketAddress tlsNetworkAddress) {
    return _ -> {
      tlsNetwork.start(tlsNetworkAddress);
      tlsNetwork.onAccept(connectionService::processAcceptedConnection);
      log.info(tlsNetworkAddress, "Started TLS MQTT network by address:[%s]"::formatted);
    };
  }
}
