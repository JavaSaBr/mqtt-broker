package javasabr.mqtt.service.impl;

import java.nio.channels.AsynchronousSocketChannel;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttConnectionFactory;
import javasabr.mqtt.network.user.NetworkMqttUserFactory;
import javasabr.rlib.network.Network;
import javasabr.rlib.network.impl.DefaultBufferAllocator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class DefaultMqttConnectionFactory implements MqttConnectionFactory<MqttConnection> {

  MqttServerConnectionConfig serverConnectionConfig;
  NetworkMqttUserFactory clientFactory;
  int maxPacketsByRead;

  @Override
  public MqttConnection newConnection(Network<MqttConnection> network, AsynchronousSocketChannel channel) {
    DefaultBufferAllocator bufferAllocator = new DefaultBufferAllocator(network.config());
    return new MqttConnection(
        network,
        channel,
        bufferAllocator,
        maxPacketsByRead,
        serverConnectionConfig,
        clientFactory);
  }
}
