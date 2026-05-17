package javasabr.mqtt.network;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.List;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.network.exception.SslProtocolException;
import javasabr.mqtt.network.ssl.SslMqttMessageReader;
import javasabr.mqtt.network.ssl.SslMqttMessageWriter;
import javasabr.mqtt.network.user.NetworkMqttUserFactory;
import javasabr.rlib.network.BufferAllocator;
import javasabr.rlib.network.Network;
import javasabr.rlib.network.packet.NetworkPacketReader;
import javasabr.rlib.network.packet.NetworkPacketWriter;
import javasabr.rlib.network.packet.WritableNetworkPacket;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@CustomLog
@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MqttSslConnection extends MqttConnection {

  SSLEngine sslEngine;

  public MqttSslConnection(
      Network<MqttConnection> network,
      AsynchronousSocketChannel channel,
      BufferAllocator bufferAllocator,
      int maxPacketsByRead,
      MqttServerConnectionConfig serverConnectionConfig,
      NetworkMqttUserFactory mqttUserFactory,
      SSLContext sslContext,
      MqttTlsProperties tlsProperties,
      boolean clientMode) {
    this.sslEngine = sslContext.createSSLEngine();
    super(network, channel, bufferAllocator, maxPacketsByRead, serverConnectionConfig, mqttUserFactory);
    this.sslEngine.setUseClientMode(clientMode);
    this.sslEngine.setNeedClientAuth(tlsProperties.requireClientCert());
    List<String> tlsProtocols = tlsProperties.tlsProtocols();
    if (!tlsProtocols.isEmpty()) {
      this.sslEngine.setEnabledProtocols(tlsProtocols.toArray(String[]::new));
    }
    List<String> cipherSuites = tlsProperties.cipherSuites();
    if (cipherSuites != null && !cipherSuites.isEmpty()) {
      this.sslEngine.setEnabledCipherSuites(cipherSuites.toArray(String[]::new));
    }
    try {
      this.sslEngine.beginHandshake();
    } catch (SSLException e) {
      throw new SslProtocolException("SSL handshake failed", e);
    }
  }

  @Override
  protected void sendImpl(WritableNetworkPacket<MqttConnection> packet) {
    super.sendImpl(packet);
  }

  protected NetworkPacketReader createPacketReader() {
    return new SslMqttMessageReader(
        this,
        this::updateLastActivity,
        this::handleReceivedValidPacket,
        this::handleReceivedInvalidPacket,
        sslEngine,
        this::sendInBackground,
        maxPacketsByRead);
  }

  protected NetworkPacketWriter createPacketWriter() {
    return new SslMqttMessageWriter(
        this,
        this::updateLastActivity,
        this::nextPacketToWrite,
        this::serializedPacket,
        this::handleSentPacket,
        sslEngine,
        this::queueAtFirst);
  }
}
