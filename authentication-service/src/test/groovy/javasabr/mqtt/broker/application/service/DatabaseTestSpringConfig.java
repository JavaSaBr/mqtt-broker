package javasabr.mqtt.broker.application.service;

import javasabr.mqtt.auth.service.config.property.Credentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseTestSpringConfig {

  @Bean
  public Credentials readerCredentials() {
    return new Credentials("user", "");
  }

  @Bean
  public Credentials adminCredentials() {
    return new Credentials("user", "");
  }
}
