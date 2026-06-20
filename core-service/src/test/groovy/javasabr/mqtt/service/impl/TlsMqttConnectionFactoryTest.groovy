package javasabr.mqtt.service.impl

import javasabr.mqtt.model.MqttServerConnectionConfig
import javasabr.mqtt.model.QoS
import javasabr.mqtt.network.TlsProperties
import javasabr.mqtt.network.message.MqttPacketCodec
import javasabr.mqtt.network.user.ConfigurableNetworkMqttUser
import javasabr.mqtt.network.user.NetworkMqttUserFactory
import javasabr.rlib.network.Network
import javasabr.rlib.network.ServerNetworkConfig
import javasabr.rlib.network.impl.DefaultBufferAllocator
import spock.lang.Specification

import javax.net.ssl.SSLContext
import java.nio.channels.AsynchronousSocketChannel

class TlsMqttConnectionFactoryTest extends Specification {

  def "should use a single shared BufferAllocator for all connections"() {
    given:
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
        def userFactory = Mock(NetworkMqttUserFactory) {
            createNetworkUser(_) >> Mock(ConfigurableNetworkMqttUser)
        }
        
        def networkConfig = ServerNetworkConfig.SimpleServerNetworkConfig.builder()
            .readBufferSize(512)
            .writeBufferSize(512)
            .pendingBufferSize(512)
            .build()
        def allocator = new DefaultBufferAllocator(networkConfig)
        
        def factory = new TlsMqttConnectionFactory(
            serverConfig,
            userFactory,
            100,
            SSLContext.getDefault(),
            TlsProperties.builder()
                .keystorePath("path")
                .keystorePassword("pass")
                .keystoreType("type")
                .tlsProtocols(["TLSv1.3"])
                .build(),
            allocator,
            new MqttPacketCodec()
        )
        
        def network = Mock(Network) {
            config() >> networkConfig
        }
        
    when:
        def conn1 = factory.newConnection(network, Mock(AsynchronousSocketChannel))
        def conn2 = factory.newConnection(network, Mock(AsynchronousSocketChannel))
        
    then:
        conn1.bufferAllocator() != null
        conn2.bufferAllocator() != null
        conn1.bufferAllocator().is(conn2.bufferAllocator())
  }
}
