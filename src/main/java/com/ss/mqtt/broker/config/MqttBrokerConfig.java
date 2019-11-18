package com.ss.mqtt.broker.config;

import com.ss.mqtt.broker.handler.packet.in.*;
import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.handler.client.MqttClientReleaseHandler;
import com.ss.mqtt.broker.network.client.MqttClient.UnsafeMqttClient;
import com.ss.mqtt.broker.network.client.DeviceMqttClient;
import com.ss.mqtt.broker.handler.client.DeviceMqttClientReleaseHandler;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.mqtt.broker.service.*;
import com.ss.mqtt.broker.service.impl.*;
import com.ss.rlib.network.BufferAllocator;
import com.ss.rlib.network.Network;
import com.ss.rlib.network.NetworkFactory;
import com.ss.rlib.network.ServerNetworkConfig;
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
public class MqttBrokerConfig {

    private interface ChannelFactory extends
        BiFunction<Network<MqttConnection>, AsynchronousSocketChannel, MqttConnection> {}

    private final Environment env;

    @Bean
    @NotNull ServerNetworkConfig networkConfig() {
        return ServerNetworkConfig.DEFAULT_SERVER;
    }

    @Bean
    @NotNull BufferAllocator bufferAllocator(@NotNull ServerNetworkConfig networkConfig) {
        return new DefaultBufferAllocator(networkConfig);
    }

    @Bean
    @NotNull ClientIdRegistry clientIdRegistry() {
        return new InMemoryClientIdRegistry(
            env.getProperty(
                "client.id.available.chars",
                "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-"
            ),
            env.getProperty("client.id.max.length", int.class, 36)
        );
    }

    @Bean
    @NotNull MqttSessionService mqttSessionService() {
        return new InMemoryMqttSessionService(
            env.getProperty("sessions.clean.thread.interval", int.class, 60000)
        );
    }

    @Bean
    @NotNull CredentialSource credentialSource() {
        return new FileCredentialsSource(env.getProperty("credentials.source.file.name", "credentials"));
    }

    @Bean
    @NotNull AuthenticationService authenticationService(@NotNull CredentialSource credentialSource) {
        return new SimpleAuthenticationService(
            credentialSource,
            env.getProperty("authentication.allow.anonymous", boolean.class, false)
        );
    }

    @Bean
    PacketInHandler @NotNull [] devicePacketHandlers(
        @NotNull AuthenticationService authenticationService,
        @NotNull ClientIdRegistry clientIdRegistry,
        @NotNull SubscriptionService subscriptionService,
        @NotNull PublishingService publishingService,
        @NotNull MqttSessionService mqttSessionService
    ) {

        var handlers = new PacketInHandler[PacketType.INVALID.ordinal()];
        handlers[PacketType.CONNECT.ordinal()] = new ConnectInPacketHandler(
            clientIdRegistry,
            authenticationService,
            mqttSessionService
        );
        handlers[PacketType.SUBSCRIBE.ordinal()] = new SubscribeInPacketHandler(subscriptionService);
        handlers[PacketType.UNSUBSCRIBE.ordinal()] = new UnsubscribeInPacketHandler(subscriptionService);
        handlers[PacketType.PUBLISH.ordinal()] = new PublishInPacketHandler(publishingService);
        handlers[PacketType.DISCONNECT.ordinal()] = new DisconnetInPacketHandler();

        return handlers;
    }

    @Bean
    @NotNull MqttClientReleaseHandler deviceMqttClientReleaseHandler(
        @NotNull ClientIdRegistry clientIdRegistry,
        @NotNull MqttSessionService mqttSessionService
    ) {
        return new DeviceMqttClientReleaseHandler(clientIdRegistry, mqttSessionService);
    }

    @Bean
    @NotNull ServerNetwork<@NotNull MqttConnection> deviceNetwork(
        @NotNull ServerNetworkConfig networkConfig,
        @NotNull BufferAllocator bufferAllocator,
        @NotNull MqttConnectionConfig deviceConnectionConfig,
        PacketInHandler @NotNull [] devicePacketHandlers,
        @NotNull MqttClientReleaseHandler deviceMqttClientReleaseHandler
    ) {
        return NetworkFactory.newServerNetwork(
            networkConfig,
            deviceConnectionFactory(
                bufferAllocator,
                deviceConnectionConfig,
                devicePacketHandlers,
                deviceMqttClientReleaseHandler
            )
        );
    }

    @Bean
    @NotNull InetSocketAddress deviceNetworkAddress(
        @NotNull ServerNetwork<@NotNull MqttConnection> deviceNetwork,
        @NotNull Consumer<@NotNull MqttConnection> mqttConnectionConsumer
    ) {

        var address = new InetSocketAddress("localhost", 1883);

        deviceNetwork.start(address);
        deviceNetwork.onAccept(mqttConnectionConsumer);

        return address;
    }

    @Bean
    @NotNull SubscriptionService subscriptionService() {
        return new SimpleSubscriptionService();
    }

    @Bean
    @NotNull PublishingService publishingService(@NotNull SubscriptionService subscriptionService) {
        return new SimplePublishingService(subscriptionService);
    }

    @Bean
    @NotNull Consumer<@NotNull MqttConnection> mqttConnectionConsumer() {
        return mqttConnection -> {
            log.info("Accepted connection: {}", mqttConnection);
            var client = (UnsafeMqttClient) mqttConnection.getClient();
            mqttConnection.onReceive((conn, packet) -> client.handle(packet));
        };
    }

    @Bean
    @NotNull MqttConnectionConfig deviceConnectionConfig() {
        return new MqttConnectionConfig(
            QoS.of(env.getProperty("mqtt.connection.max.qos", int.class, 2)),
            env.getProperty(
                "mqtt.connection.max.packet.size",
                int.class,
                MqttPropertyConstants.MAXIMUM_PACKET_SIZE_DEFAULT
            ),
            env.getProperty(
                "mqtt.connection.min.keep.alive",
                int.class,
                MqttPropertyConstants.SERVER_KEEP_ALIVE_DEFAULT
            ),
            env.getProperty(
                "mqtt.connection.receive.maximum",
                int.class,
                MqttPropertyConstants.RECEIVE_MAXIMUM_DEFAULT
            ),
            env.getProperty(
                "mqtt.connection.topic.alias.maximum",
                int.class,
                MqttPropertyConstants.TOPIC_ALIAS_MAXIMUM_DISABLED
            ),
            env.getProperty(
                "mqtt.connection.default.session.expiration.time",
                long.class,
                MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_DEFAULT
            ),
            env.getProperty(
                "mqtt.connection.keep.alive.enabled",
                boolean.class,
                MqttPropertyConstants.KEEP_ALIVE_ENABLED_DEFAULT
            ),
            env.getProperty(
                "mqtt.connection.sessions.enabled",
                boolean.class,
                MqttPropertyConstants.SESSIONS_ENABLED_DEFAULT
            ),
            env.getProperty(
                "mqtt.connection.retain.available",
                boolean.class,
                MqttPropertyConstants.RETAIN_AVAILABLE_DEFAULT
            ),
            env.getProperty(
                "mqtt.connection.wildcard.subscription.available",
                boolean.class,
                MqttPropertyConstants.WILDCARD_SUBSCRIPTION_AVAILABLE_DEFAULT
            ),
            env.getProperty(
                "mqtt.connection.subscription.id.available",
                boolean.class,
                MqttPropertyConstants.SUBSCRIPTION_IDENTIFIER_AVAILABLE_DEFAULT
            ),
            env.getProperty(
                "mqtt.connection.shared.subscription.available",
                boolean.class,
                MqttPropertyConstants.SHARED_SUBSCRIPTION_AVAILABLE_DEFAULT
            )
        );
    }

    private @NotNull ChannelFactory deviceConnectionFactory(
        @NotNull BufferAllocator bufferAllocator,
        @NotNull MqttConnectionConfig connectionConfig,
        PacketInHandler @NotNull [] packetHandlers,
        @NotNull MqttClientReleaseHandler deviceMqttClientReleaseHandler
    ) {
        return (network, channel) -> new MqttConnection(
            network,
            channel,
            bufferAllocator,
            100,
            packetHandlers,
            connectionConfig,
            mqttConnection -> new DeviceMqttClient(mqttConnection, deviceMqttClientReleaseHandler)
        );
    }
}
