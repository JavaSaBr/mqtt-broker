//file:noinspection SpringJavaInjectionPointsAutowiringInspection
package javasabr.mqtt.broker.application.config


import javasabr.mqtt.network.MqttConnection

//import javasabr.mqtt.broker.application.service.DatabaseTestSpringConfig

import javasabr.mqtt.service.ConnectionService
import javasabr.rlib.network.server.ServerNetwork
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

import java.util.concurrent.ThreadLocalRandom

@Import([
    MqttBrokerSpringConfig,
//    DatabaseTestSpringConfig
])
@Configuration(proxyBeanMethods = false)
class MqttBrokerTestConfig {

  @Bean
  InetSocketAddress externalNetworkAddress(ServerNetwork<MqttConnection> externalNetwork) {
    def random = ThreadLocalRandom.current()
    for (int i = 0; i < 100; i++) {
      def address = new InetSocketAddress("localhost", random.nextInt(800, 45000))
      try {
        externalNetwork.start(address)
        return address;
      } catch (RuntimeException ignored) {
      }
    }
    throw new RuntimeException()
  }

  @Bean
  ApplicationListener<ApplicationStartedEvent> externalNetworkStarter() {
    return (event) -> { };
  }

  @Bean
  Void startExternalNetwork(ServerNetwork<MqttConnection> externalNetwork,
                            ConnectionService connectionService,
                            InetSocketAddress externalNetworkAddress) {
    externalNetwork.onAccept(connectionService::processAcceptedConnection);
    return null
  }
}
