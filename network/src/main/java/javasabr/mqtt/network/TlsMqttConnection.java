package javasabr.mqtt.network;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.List;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.network.exception.TlsProtocolException;
import javasabr.mqtt.network.message.MqttPacketCreator;
import javasabr.mqtt.network.message.ssl.TlsMqttMessageReader;
import javasabr.mqtt.network.message.ssl.TlsMqttMessageWriter;
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
public class TlsMqttConnection extends MqttConnection {

  SSLEngine sslEngine;

  public TlsMqttConnection(
      Network<MqttConnection> network,
      AsynchronousSocketChannel channel,
      BufferAllocator bufferAllocator,
      int maxPacketsByRead,
      MqttServerConnectionConfig serverConnectionConfig,
      NetworkMqttUserFactory mqttUserFactory,
      SSLContext sslContext,
      TlsProperties tlsProperties,
      boolean clientMode,
      MqttPacketCreator mqttPacketCreator) {
    this.sslEngine = sslContext.createSSLEngine();
    super(network, channel, bufferAllocator, maxPacketsByRead, serverConnectionConfig, mqttUserFactory, mqttPacketCreator);
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
      throw new TlsProtocolException("SSL handshake failed", e);
    }
  }

  @Override
  protected void sendImpl(WritableNetworkPacket<MqttConnection> packet) {
    super.sendImpl(packet);
  }

  @Override
  protected NetworkPacketReader createPacketReader(MqttPacketCreator mqttPacketCreator) {
    return new TlsMqttMessageReader(
        this,
        this::updateLastActivity,
        this::handleReceivedValidPacket,
        this::handleReceivedInvalidPacket,
        sslEngine,
        this::sendInBackground,
        maxPacketsByRead,
        mqttPacketCreator);
  }

  @Override
  protected NetworkPacketWriter createPacketWriter() {
    return new TlsMqttMessageWriter(
        this,
        this::updateLastActivity,
        this::nextPacketToWrite,
        this::serializedPacket,
        this::handleSentPacket,
        sslEngine,
        this::queueAtFirst);
  }
}
