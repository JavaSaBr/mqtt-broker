package javasabr.mqtt.broker.application;

import javasabr.mqtt.broker.application.config.MqttBrokerSpringConfig;
import javasabr.mqtt.broker.application.config.NativeConfigurationHints;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportRuntimeHints;

@Import({
    MqttBrokerSpringConfig.class
})
@ImportRuntimeHints(NativeConfigurationHints.class)
@RequiredArgsConstructor
public class MqttBrokerApplication {
  static void main(String[] args) {
    SpringApplication.run(MqttBrokerApplication.class, args);
  }
}
