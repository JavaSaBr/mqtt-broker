package com.ss.mqtt.broker.config;

import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.mqtt.broker.network.packet.MqttWritablePacket;
import com.ss.mqtt.broker.network.packet.in.*;
import com.ss.rlib.network.*;
import com.ss.rlib.network.impl.DefaultBufferAllocator;
import com.ss.rlib.network.packet.registry.ReadablePacketRegistry;
import com.ss.rlib.network.packet.registry.impl.IdBasedReadablePacketRegistry;
import com.ss.rlib.network.server.ServerNetwork;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.function.BiFunction;

@Log4j2
@Configuration
public class MqttBrokerConfig {

    @Bean
    @NotNull ReadablePacketRegistry<MqttReadablePacket> packetRegistry() {

        var registry = new IdBasedReadablePacketRegistry<>(MqttReadablePacket.class);
        registry.register(
            AuthenticationInPacket.class,
            ConnectInPacket.class,
            DisconnectInPacket.class,
            PingRequestInPacket.class,
            PublishAckInPacket.class,
            PublishCompleteInPacket.class,
            PublishInPacket.class,
            PublishReceivedInPacket.class,
            PublishReleaseInPacket.class,
            SubscribeInPacket.class,
            UnsubscribeInPacket.class
        );

        return registry;
    }

    @Bean
    @NotNull ServerNetworkConfig networkConfig() {
        return ServerNetworkConfig.DEFAULT_SERVER;
    }

    @Bean
    @NotNull BufferAllocator bufferAllocator(@NotNull ServerNetworkConfig networkConfig) {
        return new DefaultBufferAllocator(networkConfig);
    }

    @Bean
    @NotNull Network<? extends Connection<MqttReadablePacket, MqttWritablePacket>> network(
        @NotNull ReadablePacketRegistry<MqttReadablePacket> packetRegistry,
        @NotNull ServerNetworkConfig networkConfig,
        @NotNull BufferAllocator bufferAllocator
    ) {

        ServerNetwork<MqttConnection> serverNetwork = NetworkFactory.newServerNetwork(networkConfig,
            networkChannelFactory(packetRegistry, bufferAllocator)
        );

        log.info("Start mqtt server...");

        return serverNetwork.start(new InetSocketAddress("localhost", 1883));
    }

    private @NotNull BiFunction<Network<MqttConnection>, AsynchronousSocketChannel, MqttConnection> networkChannelFactory(
        @NotNull ReadablePacketRegistry<MqttReadablePacket> packetRegistry,
        @NotNull BufferAllocator bufferAllocator
    ) {
        return (network, channel) -> new MqttConnection(network,
            channel,
            NetworkCryptor.NULL,
            bufferAllocator,
            packetRegistry,
            100,
            1,
            1
        );
    }
}
