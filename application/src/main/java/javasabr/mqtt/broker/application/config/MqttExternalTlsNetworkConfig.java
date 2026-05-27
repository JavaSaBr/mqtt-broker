package javasabr.mqtt.broker.application.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttConnectionFactory;
import javasabr.mqtt.network.TlsProperties;
import javasabr.mqtt.network.message.MqttPacketCodec;
import javasabr.mqtt.network.user.NetworkMqttUserFactory;
import javasabr.mqtt.service.ConnectionService;
import javasabr.mqtt.service.impl.TlsMqttConnectionFactory;
import javasabr.rlib.network.NetworkFactory;
import javasabr.rlib.network.ServerNetworkConfig;
import javasabr.rlib.network.impl.DefaultBufferAllocator;
import javasabr.rlib.network.server.ServerNetwork;
import javasabr.rlib.network.util.NetworkUtils;
import javax.net.ssl.SSLContext;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@CustomLog
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "mqtt.external.tls.network.enabled", havingValue = "true")
public class MqttExternalTlsNetworkConfig {

  @Bean
  TlsProperties externalTlsProperties(
      @Value("${mqtt.external.tls.keystore-path:}") String keystorePath,
      @Value("${mqtt.external.tls.keystore-password:}") String keystorePassword,
      @Value("${mqtt.external.tls.keystore-type:PKCS12}") String keystoreType,
      @Value("${mqtt.external.tls.truststore-path:}") String truststorePath,
      @Value("${mqtt.external.tls.truststore-password:}") String truststorePassword,
      @Value("${mqtt.external.tls.truststore-type:PKCS12}") String truststoreType,
      @Value("${mqtt.external.tls.require-client-cert:true}") boolean requireClientCert,
      @Value("${mqtt.external.tls.tls-protocols:#{{'TLSv1.3'}}}") List<String> tlsProtocols,
      @Value("${mqtt.external.tls.cipher-suites:#{{}}}") List<String> cipherSuites) {
    return TlsProperties.builder()
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

  @Bean("externalNetworkSslContext")
  @ConditionalOnBooleanProperty(name = "mqtt.external.tls.require-client-cert", havingValue = false)
  SSLContext externalNetworkSslContext(TlsProperties properties) throws IOException {
    String keyStoreType = properties.keystoreType();
    String keyStorePath = properties.keystorePath();
    String keyStorePassword = properties.keystorePassword();

    try (InputStream keyStoreData = Files.newInputStream(Paths.get(keyStorePath))) {
      return NetworkUtils.createSslContext(
          keyStoreType,
          keyStoreData,
          keyStorePassword,
          null,
          null,
          null);
    }
  }

  @Bean("externalNetworkSslContext")
  @ConditionalOnBooleanProperty(name = "mqtt.external.tls.require-client-cert", matchIfMissing = true)
  SSLContext externalNetworkMutualSslContext(TlsProperties properties) throws IOException {
    String keyStoreType = properties.keystoreType();
    String keyStorePath = properties.keystorePath();
    String keyStorePassword = properties.keystorePassword();

    String trustStoreType = properties.truststoreType();
    String trustStorePath = properties.truststorePath();
    String trustStorePassword = properties.truststorePassword();
    Assert.hasText(trustStoreType, "trustStoreType is blank");
    Assert.hasText(trustStorePath, "trustStorePath is blank");
    Assert.hasText(trustStorePassword, "trustStorePassword is blank");

    try (InputStream keyStoreData = Files.newInputStream(Paths.get(keyStorePath));
         InputStream trustStoreData = Files.newInputStream(Paths.get(trustStorePath))) {
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
  MqttConnectionFactory externalTlsConnectionFactory(
      MqttServerConnectionConfig externalServerConnectionConfig,
      NetworkMqttUserFactory mqttUserFactory,
      @Value("${mqtt.external.connection.max.packets.by.read:100}") int maxPacketsByRead,
      SSLContext externalNetworkSslContext,
      TlsProperties tlsProperties,
      ServerNetworkConfig externalTlsNetworkConfig,
      MqttPacketCodec mqttPacketCodec) {
    DefaultBufferAllocator defaultBufferAllocator = new DefaultBufferAllocator(externalTlsNetworkConfig);
    return new TlsMqttConnectionFactory(
        externalServerConnectionConfig,
        mqttUserFactory,
        maxPacketsByRead,
        externalNetworkSslContext,
        tlsProperties,
        defaultBufferAllocator, mqttPacketCodec);
  }

  @Bean
  ServerNetworkConfig externalTlsNetworkConfig(
      @Value("${mqtt.external.tls.network.read.buffer.size:512}") int readBufferSize,
      @Value("${mqtt.external.tls.network.pending.buffer.size:1024}") int pendingBufferSize,
      @Value("${mqtt.external.tls.network.write.buffer.size:1024}") int writeBufferSize,
      @Value("${mqtt.external.tls.network.thread.group.name:TlsNetwork}") String threadGroupName,
      @Value("${mqtt.external.tls.network.thread.count:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}") int threadGroupMaxSize) {
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
  InetSocketAddress externalTlsNetworkAddress(
      @Value("${mqtt.external.network.host:localhost}") String host,
      @Value("${mqtt.external.tls.network.port:8883}") int port) {
    return new InetSocketAddress(host, port);
  }

  @Bean
  ServerNetwork<MqttConnection> externalTlsNetwork(
      ServerNetworkConfig externalTlsNetworkConfig,
      MqttConnectionFactory externalTlsConnectionFactory) {
    return NetworkFactory.serverNetwork(externalTlsNetworkConfig, externalTlsConnectionFactory::newConnection);
  }

  @Bean
  ApplicationListener<ApplicationStartedEvent> externalTlsNetworkStarter(
      ServerNetwork<MqttConnection> externalTlsNetwork,
      ConnectionService externalMqttConnectionService,
      InetSocketAddress externalTlsNetworkAddress) {
    return _ -> {
      externalTlsNetwork.start(externalTlsNetworkAddress);
      externalTlsNetwork.onAccept(externalMqttConnectionService::processAcceptedConnection);
      log.info(externalTlsNetworkAddress, "Started TLS MQTT network by address:[%s]"::formatted);
    };
  }
}
