package javasabr.mqtt.network

import javasabr.mqtt.model.MqttServerConnectionConfig
import javasabr.mqtt.model.QoS
import javasabr.mqtt.network.message.MqttPacketCodec
import javasabr.mqtt.network.user.ConfigurableNetworkMqttUser
import javasabr.mqtt.network.user.NetworkMqttUserFactory
import javasabr.rlib.network.Network
import javasabr.rlib.network.ServerNetworkConfig
import javasabr.rlib.network.impl.DefaultBufferAllocator
import spock.lang.Specification

import javax.net.ssl.SSLContext
import java.nio.channels.AsynchronousSocketChannel

class TlsMqttConnectionTest extends Specification {

  def "should configure SSLEngine with TLS properties"() {
    given:
        def tlsConfig = TlsProperties.builder()
            .keystorePath("path")
            .keystorePassword("pass")
            .keystoreType("type")
            .requireClientCert(true)
            .truststorePath("path")
            .truststorePassword("pass")
            .truststoreType("type")
            .tlsProtocols(["TLSv1.3"])
            .build()
        def userFactory = Stub(NetworkMqttUserFactory) {
            createNetworkUser(_) >> Stub(ConfigurableNetworkMqttUser)
        }
        def serverConfig = MqttServerConnectionConfig.builder()
            .maxQos(QoS.AT_MOST_ONCE)
            .maxMessageSize(1024)
            .maxStringLength(256)
            .maxBinarySize(256)
            .maxTopicLevels(5)
            .minKeepAliveTime(10)
            .receiveMaxPublishes(10)
            .topicAliasMaxValue(10)
            .keepAliveEnabled(true)
            .sessionsEnabled(true)
            .retainAvailable(true)
            .wildcardSubscriptionAvailable(true)
            .subscriptionIdAvailable(true)
            .sharedSubscriptionAvailable(true)
            .build()
        ServerNetworkConfig tlsNetworkConfig = ServerNetworkConfig.SimpleServerNetworkConfig.builder().build();

    when:
        new TlsMqttConnection(
            Stub(Network),
            Stub(AsynchronousSocketChannel),
            new DefaultBufferAllocator(tlsNetworkConfig),
            100,
            serverConfig,
            userFactory,
            SSLContext.getDefault(),
            tlsConfig,
            false,
            new MqttPacketCodec()
        )

    then:
        noExceptionThrown()
  }
}
