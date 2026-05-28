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
class PlainNetworkTestConfig {

  @Bean
  ServerSocket externalPlainServerSocket() {
    return new ServerSocket(0)
  }

  @Bean
  InetSocketAddress externalPlainNetworkAddress(ServerSocket externalPlainServerSocket) {
    return new InetSocketAddress("localhost", externalPlainServerSocket.localPort)
  }

  @Bean
  Void testExternalNetworkStarter(
      ApplicationListener<ApplicationStartedEvent> externalNetworkStarter,
      ConfigurableApplicationContext applicationContext,
      ServerSocket externalPlainServerSocket) {
    def event = new ApplicationStartedEvent(new SpringApplication(), new String[0], applicationContext, Duration.ZERO)
    externalPlainServerSocket.close()
    externalNetworkStarter.onApplicationEvent(event)
    return null
  }
}
