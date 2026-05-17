package javasabr.mqtt.broker.application.config


import org.springframework.beans.factory.annotation.Qualifier
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
  InetSocketAddress externalNetworkAddress() {
    def socket = new ServerSocket(0)
    def port = socket.localPort
    socket.close()
    return new InetSocketAddress("localhost", port)
  }

  @Bean
  Void testExternalNetworkStarter(
      @Qualifier("externalNetworkStarter") ApplicationListener<ApplicationStartedEvent> networkStarter,
      ConfigurableApplicationContext applicationContext) {
    def event = new ApplicationStartedEvent(new SpringApplication(), new String[0], applicationContext, Duration.ZERO)
    networkStarter.onApplicationEvent(event)
    return null
  }
}
