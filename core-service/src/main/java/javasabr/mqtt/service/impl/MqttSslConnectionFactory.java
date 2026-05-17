package javasabr.mqtt.service.impl;

import java.nio.channels.AsynchronousSocketChannel;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttSslConnection;
import javasabr.mqtt.network.MqttTlsProperties;
import javasabr.mqtt.network.user.NetworkMqttUserFactory;
import javasabr.rlib.network.BufferAllocator;
import javasabr.rlib.network.Network;
import javax.net.ssl.SSLContext;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MqttSslConnectionFactory extends DefaultMqttConnectionFactory {

  SSLContext sslContext;
  MqttTlsProperties tlsPproperties;
  BufferAllocator bufferAllocator;

  public MqttSslConnectionFactory(
      MqttServerConnectionConfig serverConnectionConfig,
      NetworkMqttUserFactory clientFactory,
      int maxPacketsByRead,
      SSLContext sslContext,
      MqttTlsProperties tlsProperties,
      BufferAllocator bufferAllocator) {
    super(serverConnectionConfig, clientFactory, maxPacketsByRead);
    this.sslContext = sslContext;
    this.tlsPproperties = tlsProperties;
    this.bufferAllocator = bufferAllocator;
  }

  @Override
  public MqttSslConnection newConnection(Network<MqttConnection> network, AsynchronousSocketChannel channel) {
    return new MqttSslConnection(
        network,
        channel,
        bufferAllocator,
        maxPacketsByRead,
        serverConnectionConfig,
        clientFactory,
        sslContext,
        tlsPproperties,
        false);
  }
}
