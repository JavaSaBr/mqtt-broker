package javasabr.mqtt.broker.application.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

import java.time.Duration

@Import(MqttBrokerSpringConfig)
@Configuration(proxyBeanMethods = false)
class TlsNetworkTestConfig {

  @Bean
  ServerSocket externalTlsServerSocket() {
    return new ServerSocket(0)
  }

  @Bean
  InetSocketAddress externalTlsNetworkAddress(ServerSocket externalTlsServerSocket) {
    return new InetSocketAddress("localhost", externalTlsServerSocket.localPort)
  }

  @Bean
  Void testTlsNetworkStarter(
      ApplicationListener<ApplicationStartedEvent> externalTlsNetworkStarter,
      ConfigurableApplicationContext applicationContext,
      ServerSocket externalTlsServerSocket) {
    def event = new ApplicationStartedEvent(new SpringApplication(), new String[0], applicationContext, Duration.ZERO)
    externalTlsServerSocket.close()
    externalTlsNetworkStarter.onApplicationEvent(event)
    return null
  }
}
