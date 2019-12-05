package com.ss.mqtt.broker.config;

import com.ss.mqtt.broker.handler.client.MqttClientReleaseHandler;
import com.ss.mqtt.broker.handler.packet.in.*;
import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.client.ExternalMqttClient;
import com.ss.mqtt.broker.network.client.InternalMqttClient;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.rlib.network.BufferAllocator;
import com.ss.rlib.network.Network;
import com.ss.rlib.network.NetworkFactory;
import com.ss.rlib.network.ServerNetworkConfig;
import com.ss.rlib.network.ServerNetworkConfig.SimpleServerNetworkConfig;
import com.ss.rlib.network.impl.DefaultBufferAllocator;
import com.ss.rlib.network.server.ServerNetwork;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class MqttNetworkConfig {

    private interface ChannelFactory extends
        BiFunction<Network<MqttConnection>, AsynchronousSocketChannel, MqttConnection> {}

    private final Environment env;

    @Bean
    @NotNull ServerNetworkConfig internalNetworkConfig() {
        return SimpleServerNetworkConfig.builder()
            .readBufferSize(env.getProperty("mqtt.internal.network.read.buffer.size", int.class, 2048))
            .pendingBufferSize(env.getProperty("mqtt.internal.network.pending.buffer.size", int.class, 4096))
            .writeBufferSize(env.getProperty("mqtt.internal.network.write.buffer.size", int.class, 2048))
            .threadGroupName("InternalNetwork")
            .threadGroupSize(env.getProperty("mqtt.internal.network.thread.count", int.class, 1))
            .build();
    }

    @Bean
    @NotNull ServerNetworkConfig externalNetworkConfig() {
        return SimpleServerNetworkConfig.builder()
            .readBufferSize(env.getProperty("mqtt.external.network.read.buffer.size", int.class, 100))
            .pendingBufferSize(env.getProperty("mqtt.external.network.pending.buffer.size", int.class, 200))
            .writeBufferSize(env.getProperty("mqtt.external.network.write.buffer.size", int.class, 100))
            .threadGroupName("ExternalNetwork")
            .threadGroupSize(env.getProperty("mqtt.external.network.thread.count", int.class, 1))
            .build();
    }

    @Bean
    @NotNull BufferAllocator internalBufferAllocator(@NotNull ServerNetworkConfig internalNetworkConfig) {
        return new DefaultBufferAllocator(internalNetworkConfig);
    }

    @Bean
    @NotNull BufferAllocator externalBufferAllocator(@NotNull ServerNetworkConfig externalNetworkConfig) {
        return new DefaultBufferAllocator(externalNetworkConfig);
    }

    @Bean
    @NotNull ServerNetwork<@NotNull MqttConnection> externalNetwork(
        @NotNull ServerNetworkConfig externalNetworkConfig,
        @NotNull BufferAllocator externalBufferAllocator,
        @NotNull MqttConnectionConfig externalConnectionConfig,
        PacketInHandler @NotNull [] packetHandlers,
        @NotNull MqttClientReleaseHandler mqttClientReleaseHandler
    ) {
        return NetworkFactory.newServerNetwork(
            externalNetworkConfig,
            externalConnectionFactory(
                externalBufferAllocator,
                externalConnectionConfig,
                packetHandlers,
                mqttClientReleaseHandler
            )
        );
    }

    @Bean
    @NotNull ServerNetwork<@NotNull MqttConnection> internalNetwork(
        @NotNull ServerNetworkConfig internalNetworkConfig,
        @NotNull BufferAllocator internalBufferAllocator,
        @NotNull MqttConnectionConfig internalConnectionConfig,
        PacketInHandler @NotNull [] packetHandlers,
        @NotNull MqttClientReleaseHandler mqttClientReleaseHandler
    ) {
        return NetworkFactory.newServerNetwork(
            internalNetworkConfig,
            internalConnectionFactory(
                internalBufferAllocator,
                internalConnectionConfig,
                packetHandlers,
                mqttClientReleaseHandler
            )
        );
    }

    @Bean
    @NotNull InetSocketAddress externalNetworkAddress(
        @NotNull ServerNetwork<@NotNull MqttConnection> externalNetwork,
        @NotNull Consumer<@NotNull MqttConnection> externalConnectionConsumer
    ) {

        var address = new InetSocketAddress(
            env.getProperty("mqtt.external.network.host", "localhost"),
            env.getProperty("mqtt.external.network.port", int.class, 1883)
        );

        externalNetwork.start(address);
        externalNetwork.onAccept(externalConnectionConsumer);

        return address;
    }

    @Bean
    @NotNull InetSocketAddress internalNetworkAddress(
        @NotNull ServerNetwork<@NotNull MqttConnection> internalNetwork,
        @NotNull Consumer<@NotNull MqttConnection> internalConnectionConsumer
    ) {

        var address = new InetSocketAddress(
            env.getProperty("mqtt.internal.network.host", "localhost"),
            env.getProperty("mqtt.internal.network.port", int.class, 11883)
        );

        internalNetwork.start(address);
        internalNetwork.onAccept(internalConnectionConsumer);

        return address;
    }

    @Bean
    @NotNull Consumer<@NotNull MqttConnection> externalConnectionConsumer() {
        return mqttConnection -> {
            log.info("Accepted external connection: {}", mqttConnection);
            var client = (UnsafeMqttClient) mqttConnection.getClient();
            mqttConnection.onReceive((conn, packet) -> client.handle(packet));
        };
    }

    @Bean
    @NotNull Consumer<@NotNull MqttConnection> internalConnectionConsumer() {
        return mqttConnection -> {
            log.info("Accepted internal connection: {}", mqttConnection);
            var client = (UnsafeMqttClient) mqttConnection.getClient();
            mqttConnection.onReceive((conn, packet) -> client.handle(packet));
        };
    }

    @Bean
    @NotNull MqttConnectionConfig externalConnectionConfig() {
        return new MqttConnectionConfig(
            QoS.of(env.getProperty("mqtt.connection.max.qos", int.class, 2)),
            env.getProperty(
                "mqtt.external.connection.max.packet.size",
                int.class,
                MqttPropertyConstants.MAXIMUM_PACKET_SIZE_DEFAULT
            ),
            env.getProperty(
                "mqtt.external.connection.min.keep.alive",
                int.class,
                MqttPropertyConstants.SERVER_KEEP_ALIVE_DEFAULT
            ),
            env.getProperty(
                "mqtt.external.connection.receive.maximum",
                int.class,
                MqttPropertyConstants.RECEIVE_MAXIMUM_DEFAULT
            ),
            env.getProperty(
                "mqtt.external.connection.topic.alias.maximum",
                int.class,
                MqttPropertyConstants.TOPIC_ALIAS_MAXIMUM_DISABLED
            ),
            env.getProperty(
                "mqtt.external.connection.default.session.expiration.time",
                long.class,
                MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_DEFAULT
            ),
            env.getProperty(
                "mqtt.external.connection.keep.alive.enabled",
                boolean.class,
                MqttPropertyConstants.KEEP_ALIVE_ENABLED_DEFAULT
            ),
            env.getProperty(
                "mqtt.external.connection.sessions.enabled",
                boolean.class,
                MqttPropertyConstants.SESSIONS_ENABLED_DEFAULT
            ),
            env.getProperty(
                "mqtt.external.connection.retain.available",
                boolean.class,
                MqttPropertyConstants.RETAIN_AVAILABLE_DEFAULT
            ),
            env.getProperty(
                "mqtt.external.connection.wildcard.subscription.available",
                boolean.class,
                MqttPropertyConstants.WILDCARD_SUBSCRIPTION_AVAILABLE_DEFAULT
            ),
            env.getProperty(
                "mqtt.external.connection.subscription.id.available",
                boolean.class,
                MqttPropertyConstants.SUBSCRIPTION_IDENTIFIER_AVAILABLE_DEFAULT
            ),
            env.getProperty(
                "mqtt.external.connection.shared.subscription.available",
                boolean.class,
                MqttPropertyConstants.SHARED_SUBSCRIPTION_AVAILABLE_DEFAULT
            )
        );
    }

    @Bean
    @NotNull MqttConnectionConfig internalConnectionConfig() {
        return new MqttConnectionConfig(
            QoS.of(env.getProperty("mqtt.internal.connection.max.qos", int.class, 2)),
            env.getProperty(
                "mqtt.internal.connection.max.packet.size",
                int.class,
                MqttPropertyConstants.MAXIMUM_PACKET_SIZE_DEFAULT
            ),
            env.getProperty(
                "mqtt.internal.connection.min.keep.alive",
                int.class,
                MqttPropertyConstants.SERVER_KEEP_ALIVE_DEFAULT
            ),
            env.getProperty(
                "mqtt.internal.connection.receive.maximum",
                int.class,
                MqttPropertyConstants.RECEIVE_MAXIMUM_DEFAULT
            ),
            env.getProperty(
                "mqtt.internal.connection.topic.alias.maximum",
                int.class,
                MqttPropertyConstants.TOPIC_ALIAS_MAXIMUM_DISABLED
            ),
            env.getProperty(
                "mqtt.internal.connection.default.session.expiration.time",
                long.class,
                MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_DEFAULT
            ),
            env.getProperty(
                "mqtt.internal.connection.keep.alive.enabled",
                boolean.class,
                MqttPropertyConstants.KEEP_ALIVE_ENABLED_DEFAULT
            ),
            env.getProperty(
                "mqtt.internal.connection.sessions.enabled",
                boolean.class,
                MqttPropertyConstants.SESSIONS_ENABLED_DEFAULT
            ),
            env.getProperty(
                "mqtt.internal.connection.retain.available",
                boolean.class,
                MqttPropertyConstants.RETAIN_AVAILABLE_DEFAULT
            ),
            env.getProperty(
                "mqtt.internal.connection.wildcard.subscription.available",
                boolean.class,
                MqttPropertyConstants.WILDCARD_SUBSCRIPTION_AVAILABLE_DEFAULT
            ),
            env.getProperty(
                "mqtt.internal.connection.subscription.id.available",
                boolean.class,
                MqttPropertyConstants.SUBSCRIPTION_IDENTIFIER_AVAILABLE_DEFAULT
            ),
            env.getProperty(
                "mqtt.internal.connection.shared.subscription.available",
                boolean.class,
                MqttPropertyConstants.SHARED_SUBSCRIPTION_AVAILABLE_DEFAULT
            )
        );
    }

    private @NotNull ChannelFactory externalConnectionFactory(
        @NotNull BufferAllocator bufferAllocator,
        @NotNull MqttConnectionConfig connectionConfig,
        PacketInHandler @NotNull [] packetHandlers,
        @NotNull MqttClientReleaseHandler releaseHandler
    ) {
        return connectionFactory(
            bufferAllocator,
            connectionConfig,
            packetHandlers,
            releaseHandler,
            ExternalMqttClient::new
        );
    }

    private @NotNull ChannelFactory internalConnectionFactory(
        @NotNull BufferAllocator bufferAllocator,
        @NotNull MqttConnectionConfig connectionConfig,
        PacketInHandler @NotNull [] packetHandlers,
        @NotNull MqttClientReleaseHandler releaseHandler
    ) {
        return connectionFactory(
            bufferAllocator,
            connectionConfig,
            packetHandlers,
            releaseHandler,
            InternalMqttClient::new
        );
    }

    private @NotNull ChannelFactory connectionFactory(
        @NotNull BufferAllocator bufferAllocator,
        @NotNull MqttConnectionConfig connectionConfig,
        PacketInHandler @NotNull [] packetHandlers,
        @NotNull MqttClientReleaseHandler releaseHandler,
        @NotNull BiFunction<MqttConnection, MqttClientReleaseHandler, UnsafeMqttClient> clientFactory
    ) {
        return (network, channel) -> new MqttConnection(
            network,
            channel,
            bufferAllocator,
            100,
            packetHandlers,
            connectionConfig,
            mqttConnection -> clientFactory.apply(mqttConnection, releaseHandler)
        );
    }
}
