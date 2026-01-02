package javasabr.mqtt.broker.application.service;

import javasabr.mqtt.auth.service.config.property.DatabaseCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseTestSpringConfig {

  @Bean
  public DatabaseCredentials readerDatabaseCredentials() {
    return new DatabaseCredentials("user", "");
  }

  @Bean
  public DatabaseCredentials adminDatabaseCredentials() {
    return new DatabaseCredentials("user", "");
  }
}
