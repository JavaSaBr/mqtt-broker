package com.ss.mqtt.broker.config;

import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import com.ss.mqtt.broker.network.packet.out.MqttWritablePacket;
import com.ss.mqtt.broker.service.ClientService;
import com.ss.mqtt.broker.service.PublishingService;
import com.ss.mqtt.broker.service.SubscriptionService;
import com.ss.mqtt.broker.service.impl.DefaultClientService;
import com.ss.mqtt.broker.service.impl.SimplePublishingService;
import com.ss.mqtt.broker.service.impl.SimpleSubscriptionService;
import com.ss.mqtt.broker.service.impl.SimpleSubscriptions;
import com.ss.rlib.network.*;
import com.ss.rlib.network.impl.DefaultBufferAllocator;
import com.ss.rlib.network.server.ServerNetwork;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Log4j2
@Configuration
public class MqttBrokerConfig {

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

    @Bean
    @NotNull Network<? extends Connection<MqttReadablePacket, MqttWritablePacket>> network(
        @NotNull ServerNetworkConfig networkConfig,
        @NotNull BufferAllocator bufferAllocator,
        @NotNull Consumer<MqttConnection> mqttConnectionConsumer
    ) {
        ServerNetwork<MqttConnection> serverNetwork = NetworkFactory.newServerNetwork(
            networkConfig,
            networkChannelFactory(bufferAllocator)
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
    @NotNull PublishingService publishingService() {
        return new SimplePublishingService(subscriptionService());
    }

    @Bean
    @NotNull Consumer<MqttConnection> mqttConnectionConsumer(@NotNull ClientService clientService) {
        return mqttConnection -> {
            log.info("Accepted connection: {}", mqttConnection);
            var client = mqttConnection.getClient();
            mqttConnection.onReceive((conn, packet) -> client.handle(packet));
        };
    }

    private @NotNull BiFunction<Network<MqttConnection>, AsynchronousSocketChannel, MqttConnection> networkChannelFactory(
        @NotNull BufferAllocator bufferAllocator
    ) {
        return (network, channel) -> new MqttConnection(network,
            channel,
            NetworkCryptor.NULL,
            bufferAllocator,
            100,
            subscriptionService(),
            publishingService()
        );
    }
}
