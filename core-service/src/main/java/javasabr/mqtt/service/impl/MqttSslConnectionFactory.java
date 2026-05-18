package javasabr.mqtt.service.impl;

import java.nio.channels.AsynchronousSocketChannel;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttSslConnection;
import javasabr.mqtt.network.TlsProperties;
import javasabr.mqtt.network.message.MqttPacketCreator;
import javasabr.mqtt.network.user.NetworkMqttUserFactory;
import javasabr.rlib.network.BufferAllocator;
import javasabr.rlib.network.Network;
import javax.net.ssl.SSLContext;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MqttSslConnectionFactory extends PlainMqttConnectionFactory {

  SSLContext sslContext;
  TlsProperties tlsPproperties;
  BufferAllocator bufferAllocator;
  MqttPacketCreator mqttPacketCreator;

  public MqttSslConnectionFactory(
      MqttServerConnectionConfig serverConnectionConfig,
      NetworkMqttUserFactory clientFactory,
      int maxPacketsByRead,
      SSLContext sslContext,
      TlsProperties tlsProperties,
      BufferAllocator bufferAllocator,
      MqttPacketCreator mqttPacketCreator) {
    super(serverConnectionConfig, clientFactory, maxPacketsByRead, mqttPacketCreator);
    this.sslContext = sslContext;
    this.tlsPproperties = tlsProperties;
    this.bufferAllocator = bufferAllocator;
    this.mqttPacketCreator = mqttPacketCreator;
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
        false,
        mqttPacketCreator);
  }
}
