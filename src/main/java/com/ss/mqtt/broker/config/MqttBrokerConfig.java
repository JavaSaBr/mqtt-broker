package com.ss.mqtt.broker.config;

import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.QoS;
import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.client.UnsafeMqttClient;
import com.ss.mqtt.broker.network.client.impl.DeviceMqttClient;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import com.ss.mqtt.broker.network.packet.in.handler.ConnectInPacketHandler;
import com.ss.mqtt.broker.network.packet.in.handler.PacketInHandler;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import com.ss.mqtt.broker.service.ClientIdRegistry;
import com.ss.mqtt.broker.service.ClientService;
import com.ss.mqtt.broker.service.SubscriptionService;
import com.ss.mqtt.broker.service.impl.DefaultClientService;
import com.ss.mqtt.broker.service.impl.SimpleClientIdRegistry;
import com.ss.mqtt.broker.service.impl.SimpleSubscriptionService;
import com.ss.mqtt.broker.service.impl.SimpleSubscriptions;
import com.ss.rlib.network.*;
import com.ss.rlib.network.impl.DefaultBufferAllocator;
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
    @NotNull ClientService clientService() {
        return new DefaultClientService();
    }

    @NotNull
    @Bean ClientIdRegistry clientIdRegistry() {
        return new SimpleClientIdRegistry();
    }

    @Bean
    PacketInHandler @NotNull [] devicePacketHandlers(
        @NotNull ClientIdRegistry clientIdRegistry
    ) {

        var handlers = new PacketInHandler[PacketType.INVALID.ordinal()];
        handlers[PacketType.CONNECT.ordinal()] = new ConnectInPacketHandler(clientIdRegistry);

        return handlers;
    }
    @Bean
    @NotNull Network<? extends Connection<MqttReadablePacket, MqttWritablePacket>> deviceNetwork(
        @NotNull ServerNetworkConfig networkConfig,
        @NotNull BufferAllocator bufferAllocator,
        @NotNull Consumer<MqttConnection> mqttConnectionConsumer,
        @NotNull MqttConnectionConfig deviceConnectionConfig,
        @NotNull SubscriptionService subscriptionService,
        PacketInHandler @NotNull [] devicePacketHandlers
    ) {

        var serverNetwork = NetworkFactory.newServerNetwork(
            networkConfig,
            deviceConnectionFactory(
                bufferAllocator,
                deviceConnectionConfig,
                subscriptionService,
                devicePacketHandlers
            )
        );
        serverNetwork.start(new InetSocketAddress("localhost", 1883));
        serverNetwork.onAccept(mqttConnectionConsumer);

        return serverNetwork;
    }

    @Bean
    @NotNull SubscriptionService subscriptionService() {
        return new SimpleSubscriptionService(new SimpleSubscriptions());
    }

    @Bean
    @NotNull Consumer<MqttConnection> mqttConnectionConsumer() {
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
                MqttPropertyConstants.SUBSCRIPTION_IDENTIFIER_AVAILABLE
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
        @NotNull SubscriptionService subscriptionService,
        PacketInHandler @NotNull [] packetHandlers
    ) {
        return (network, channel) -> new MqttConnection(
            network,
            channel,
            bufferAllocator,
            100,
            packetHandlers,
            subscriptionService,
            connectionConfig,
            DeviceMqttClient::new
        );
    }
}
