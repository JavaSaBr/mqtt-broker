package javasabr.mqtt.broker.application

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import groovy.util.logging.Slf4j
import javasabr.mqtt.broker.application.config.TlsNetworkTestConfig
import javasabr.mqtt.test.support.BaseSpecification
import javasabr.mqtt.test.support.TestSslContexts
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource

@ContextConfiguration(
    classes = [TlsNetworkTestConfig],
    initializers = [TestSslPropertiesInitializer])
@TestPropertySource(
    properties = ["mqtt.external.tls.network.enabled=true", "mqtt.external.tls.require-client-cert=false"],
    locations = "classpath:application-test.properties")
@Slf4j
class TlsIntegrationSpecification extends BaseSpecification {

  static TestSslContexts sslContexts = TestSslContexts.getInstance()

  @Autowired(required = false)
  InetSocketAddress externalTlsNetworkAddress

  Mqtt5AsyncClient buildTlsMqtt5Client() {
    return MqttClient.builder()
        .identifier(MqttClientFactory.generateClientId("TLS5"))
        .serverHost(externalTlsNetworkAddress.hostName)
        .serverPort(externalTlsNetworkAddress.port)
        .sslConfig()
        .trustManagerFactory(TlsIntegrationSpecification.sslContexts.buildTrustManagerFactory())
        .applySslConfig()
        .useMqttVersion5()
        .addDisconnectedListener { log.info "[TLS/mqtt5] disconnected: ${it.cause.message}" }
        .buildAsync()
  }

  Mqtt3AsyncClient buildTlsMqtt311Client() {
    return MqttClient.builder()
        .identifier(MqttClientFactory.generateClientId("TLS3"))
        .serverHost(externalTlsNetworkAddress.hostName)
        .serverPort(externalTlsNetworkAddress.port)
        .sslConfig()
        .trustManagerFactory(sslContexts.buildTrustManagerFactory())
        .applySslConfig()
        .useMqttVersion3()
        .addDisconnectedListener { log.info "[TLS/mqtt311] disconnected: ${it.cause.message}" }
        .buildAsync()
  }
}
