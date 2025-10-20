package javasabr.mqtt.application.config;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.client.ExternalMqttClient;
import javasabr.mqtt.network.client.InternalMqttClient;
import javasabr.mqtt.network.handler.MqttClientReleaseHandler;
import javasabr.mqtt.network.handler.PacketInHandler;
import javasabr.mqtt.network.packet.in.MqttReadablePacket;
import javasabr.rlib.network.BufferAllocator;
import javasabr.rlib.network.Network;
import javasabr.rlib.network.NetworkFactory;
import javasabr.rlib.network.ServerNetworkConfig;
import javasabr.rlib.network.ServerNetworkConfig.SimpleServerNetworkConfig;
import javasabr.rlib.network.impl.DefaultBufferAllocator;
import javasabr.rlib.network.server.ServerNetwork;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@CustomLog
@Configuration
@RequiredArgsConstructor
public class MqttNetworkConfig {

  private interface ChannelFactory extends
      BiFunction<Network<MqttConnection>, AsynchronousSocketChannel, MqttConnection> {}

  private final Environment env;

  @Bean
  ServerNetworkConfig internalNetworkConfig() {
    return SimpleServerNetworkConfig
        .builder()
        .readBufferSize(env.getProperty("mqtt.internal.network.read.buffer.size", int.class, 2048))
        .pendingBufferSize(env.getProperty("mqtt.internal.network.pending.buffer.size", int.class, 4096))
        .writeBufferSize(env.getProperty("mqtt.internal.network.write.buffer.size", int.class, 2048))
        .threadGroupName("InternalNetwork")
        .threadGroupMaxSize(env.getProperty("mqtt.internal.network.thread.count", int.class, 1))
        .build();
  }

  @Bean
  ServerNetworkConfig externalNetworkConfig() {
    return SimpleServerNetworkConfig
        .builder()
        .readBufferSize(env.getProperty("mqtt.external.network.read.buffer.size", int.class, 100))
        .pendingBufferSize(env.getProperty("mqtt.external.network.pending.buffer.size", int.class, 200))
        .writeBufferSize(env.getProperty("mqtt.external.network.write.buffer.size", int.class, 100))
        .threadGroupName("ExternalNetwork")
        .threadGroupMaxSize(env.getProperty("mqtt.external.network.thread.count", int.class, 1))
        .build();
  }

  @Bean
  BufferAllocator internalBufferAllocator(ServerNetworkConfig internalNetworkConfig) {
    return new DefaultBufferAllocator(internalNetworkConfig);
  }

  @Bean
  BufferAllocator externalBufferAllocator(ServerNetworkConfig externalNetworkConfig) {
    return new DefaultBufferAllocator(externalNetworkConfig);
  }

  @Bean
  ServerNetwork<MqttConnection> externalNetwork(
      ServerNetworkConfig externalNetworkConfig,
      BufferAllocator externalBufferAllocator,
      MqttServerConnectionConfig externalConnectionConfig,
      PacketInHandler[] packetHandlers,
      MqttClientReleaseHandler mqttClientReleaseHandler) {
    return NetworkFactory.serverNetwork(
        externalNetworkConfig,
        externalConnectionFactory(
            externalBufferAllocator,
            externalConnectionConfig,
            packetHandlers,
            mqttClientReleaseHandler));
  }

  @Bean
  ServerNetwork<MqttConnection> internalNetwork(
      ServerNetworkConfig internalNetworkConfig,
      BufferAllocator internalBufferAllocator,
      MqttServerConnectionConfig internalConnectionConfig,
      PacketInHandler[] packetHandlers,
      MqttClientReleaseHandler mqttClientReleaseHandler) {
    return NetworkFactory.serverNetwork(
        internalNetworkConfig,
        internalConnectionFactory(
            internalBufferAllocator,
            internalConnectionConfig,
            packetHandlers,
            mqttClientReleaseHandler));
  }

  @Bean
  InetSocketAddress externalNetworkAddress(
      ServerNetwork<MqttConnection> externalNetwork,
      Consumer<MqttConnection> externalConnectionConsumer) {

    var address = new InetSocketAddress(
        env.getProperty("mqtt.external.network.host", "localhost"),
        env.getProperty("mqtt.external.network.port", int.class, 1883));

    externalNetwork.start(address);
    externalNetwork.onAccept(externalConnectionConsumer);

    return address;
  }

  @Bean
  InetSocketAddress internalNetworkAddress(
      ServerNetwork<MqttConnection> internalNetwork,
      Consumer<MqttConnection> internalConnectionConsumer) {

    var address = new InetSocketAddress(
        env.getProperty("mqtt.internal.network.host", "localhost"),
        env.getProperty("mqtt.internal.network.port", int.class, 11883));

    internalNetwork.start(address);
    internalNetwork.onAccept(internalConnectionConsumer);

    return address;
  }

  @Bean
  Consumer<MqttConnection> externalConnectionConsumer() {
    return mqttConnection -> {
      log.info(mqttConnection.remoteAddress(), "[%s] Accepted external connection"::formatted);
      var client = (UnsafeMqttClient) mqttConnection.client();
      mqttConnection.onReceive((conn, packet) -> client.handle((MqttReadablePacket) packet));
    };
  }

  @Bean
  Consumer<MqttConnection> internalConnectionConsumer() {
    return mqttConnection -> {
      log.info(mqttConnection.remoteAddress(), "[%s] Accepted internal connection"::formatted);
      var client = (UnsafeMqttClient) mqttConnection.client();
      mqttConnection.onReceive((conn, packet) -> client.handle((MqttReadablePacket) packet));
    };
  }

  @Bean
  MqttServerConnectionConfig externalConnectionConfig() {
    return new MqttServerConnectionConfig(
        QoS.of(env.getProperty("mqtt.connection.max.qos", int.class, 2)),
        env.getProperty(
            "mqtt.external.connection.max.packet.size",
            int.class,
            MqttProperties.MAXIMUM_PACKET_SIZE_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.max.string.length",
            int.class,
            MqttProperties.MAXIMUM_STRING_LENGTH),
        env.getProperty(
            "mqtt.external.connection.max.binary.size",
            int.class,
            MqttProperties.MAXIMUM_BINARY_SIZE),
        env.getProperty(
            "mqtt.external.connection.min.keep.alive",
            int.class,
            MqttProperties.SERVER_KEEP_ALIVE_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.receive.maximum",
            int.class,
            MqttProperties.RECEIVE_MAXIMUM_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.topic.alias.maximum",
            int.class,
            MqttProperties.TOPIC_ALIAS_MAXIMUM_DISABLED),
        env.getProperty(
            "mqtt.external.connection.default.session.expiration.time",
            long.class,
            MqttProperties.SESSION_EXPIRY_INTERVAL_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.keep.alive.enabled",
            boolean.class,
            MqttProperties.KEEP_ALIVE_ENABLED_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.sessions.enabled",
            boolean.class,
            MqttProperties.SESSIONS_ENABLED_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.retain.available",
            boolean.class,
            MqttProperties.RETAIN_AVAILABLE_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.wildcard.subscription.available",
            boolean.class,
            MqttProperties.WILDCARD_SUBSCRIPTION_AVAILABLE_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.subscription.id.available",
            boolean.class,
            MqttProperties.SUBSCRIPTION_IDENTIFIER_AVAILABLE_DEFAULT),
        env.getProperty(
            "mqtt.external.connection.shared.subscription.available",
            boolean.class,
            MqttProperties.SHARED_SUBSCRIPTION_AVAILABLE_DEFAULT));
  }

  @Bean
  MqttServerConnectionConfig internalConnectionConfig() {
    return new MqttServerConnectionConfig(
        QoS.of(env.getProperty("mqtt.internal.connection.max.qos", int.class, 2)),
        env.getProperty(
            "mqtt.internal.connection.max.packet.size",
            int.class,
            MqttProperties.MAXIMUM_PACKET_SIZE_DEFAULT),
        env.getProperty(
            "mqtt.internal.connection.max.string.length",
            int.class,
            MqttProperties.MAXIMUM_STRING_LENGTH),
        env.getProperty(
            "mqtt.internal.connection.max.binary.size",
            int.class,
            MqttProperties.MAXIMUM_BINARY_SIZE),
        env.getProperty(
            "mqtt.internal.connection.min.keep.alive",
            int.class,
            MqttProperties.SERVER_KEEP_ALIVE_DEFAULT),
        env.getProperty(
            "mqtt.internal.connection.receive.maximum",
            int.class,
            MqttProperties.RECEIVE_MAXIMUM_DEFAULT),
        env.getProperty(
            "mqtt.internal.connection.topic.alias.maximum",
            int.class,
            MqttProperties.TOPIC_ALIAS_MAXIMUM_DISABLED),
        env.getProperty(
            "mqtt.internal.connection.default.session.expiration.time",
            long.class,
            MqttProperties.SESSION_EXPIRY_INTERVAL_DEFAULT),
        env.getProperty(
            "mqtt.internal.connection.keep.alive.enabled",
            boolean.class,
            MqttProperties.KEEP_ALIVE_ENABLED_DEFAULT),
        env.getProperty(
            "mqtt.internal.connection.sessions.enabled",
            boolean.class,
            MqttProperties.SESSIONS_ENABLED_DEFAULT),
        env.getProperty(
            "mqtt.internal.connection.retain.available",
            boolean.class,
            MqttProperties.RETAIN_AVAILABLE_DEFAULT),
        env.getProperty(
            "mqtt.internal.connection.wildcard.subscription.available",
            boolean.class,
            MqttProperties.WILDCARD_SUBSCRIPTION_AVAILABLE_DEFAULT),
        env.getProperty(
            "mqtt.internal.connection.subscription.id.available",
            boolean.class,
            MqttProperties.SUBSCRIPTION_IDENTIFIER_AVAILABLE_DEFAULT),
        env.getProperty(
            "mqtt.internal.connection.shared.subscription.available",
            boolean.class,
            MqttProperties.SHARED_SUBSCRIPTION_AVAILABLE_DEFAULT));
  }

  private ChannelFactory externalConnectionFactory(
      BufferAllocator bufferAllocator,
      MqttServerConnectionConfig connectionConfig,
      PacketInHandler[] packetHandlers,
      MqttClientReleaseHandler releaseHandler) {
    return connectionFactory(
        bufferAllocator,
        connectionConfig,
        packetHandlers,
        releaseHandler,
        ExternalMqttClient::new);
  }

  private ChannelFactory internalConnectionFactory(
      BufferAllocator bufferAllocator,
      MqttServerConnectionConfig connectionConfig,
      PacketInHandler[] packetHandlers,
      MqttClientReleaseHandler releaseHandler) {
    return connectionFactory(
        bufferAllocator,
        connectionConfig,
        packetHandlers,
        releaseHandler,
        InternalMqttClient::new);
  }

  private ChannelFactory connectionFactory(
      BufferAllocator bufferAllocator,
      MqttServerConnectionConfig connectionConfig,
      PacketInHandler[] packetHandlers,
      MqttClientReleaseHandler releaseHandler,
      BiFunction<MqttConnection, MqttClientReleaseHandler, UnsafeMqttClient> clientFactory) {
    return (network, channel) -> new MqttConnection(
        network,
        channel,
        bufferAllocator,
        100,
        packetHandlers,
        connectionConfig,
        mqttConnection -> clientFactory.apply(mqttConnection, releaseHandler));
  }
}
