package javasabr.mqtt.service.impl;

import java.nio.channels.AsynchronousSocketChannel;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.TlsMqttConnection;
import javasabr.mqtt.network.TlsProperties;
import javasabr.mqtt.network.message.MqttPacketCodec;
import javasabr.mqtt.network.user.NetworkMqttUserFactory;
import javasabr.rlib.network.BufferAllocator;
import javasabr.rlib.network.Network;
import javax.net.ssl.SSLContext;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TlsMqttConnectionFactory extends PlainMqttConnectionFactory {

  SSLContext sslContext;
  TlsProperties tlsProperties;
  BufferAllocator bufferAllocator;
  MqttPacketCodec mqttPacketCodec;

  public TlsMqttConnectionFactory(
      MqttServerConnectionConfig serverConnectionConfig,
      NetworkMqttUserFactory clientFactory,
      int maxPacketsByRead,
      SSLContext sslContext,
      TlsProperties tlsProperties,
      BufferAllocator bufferAllocator,
      MqttPacketCodec mqttPacketCodec) {
    super(serverConnectionConfig, clientFactory, maxPacketsByRead, mqttPacketCodec);
    this.sslContext = sslContext;
    this.tlsProperties = tlsProperties;
    this.bufferAllocator = bufferAllocator;
    this.mqttPacketCodec = mqttPacketCodec;
  }

  @Override
  public TlsMqttConnection newConnection(Network<MqttConnection> network, AsynchronousSocketChannel channel) {
    return new TlsMqttConnection(
        network,
        channel,
        bufferAllocator,
        maxPacketsByRead,
        serverConnectionConfig,
        clientFactory,
        sslContext,
        tlsProperties,
        false, mqttPacketCodec);
  }
}
