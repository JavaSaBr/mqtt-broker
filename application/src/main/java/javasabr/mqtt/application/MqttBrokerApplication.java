package javasabr.mqtt.application;

import javasabr.mqtt.application.config.MqttBrokerConfig;
import javasabr.mqtt.application.config.MqttNetworkConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@RequiredArgsConstructor
@Import({
    MqttBrokerConfig.class,
    MqttNetworkConfig.class
})
public class MqttBrokerApplication {
  static void main(String[] args) {
    SpringApplication.run(MqttBrokerApplication.class, args);
  }
}
