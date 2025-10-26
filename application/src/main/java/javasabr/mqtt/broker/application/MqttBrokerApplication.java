package javasabr.mqtt.broker.application;

import javasabr.mqtt.broker.application.config.MqttBrokerSpringConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Import;

@Import({
    MqttBrokerSpringConfig.class
})
@RequiredArgsConstructor
public class MqttBrokerApplication {
  static void main(String[] args) {
    SpringApplication.run(MqttBrokerApplication.class, args);
  }
}
