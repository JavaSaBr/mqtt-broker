package javasabr.mqtt.auth.service.config;

import javasabr.mqtt.auth.api.CredentialsSource;
import javasabr.mqtt.auth.api.DatabaseConnectionProperties;
import javasabr.mqtt.auth.api.DatabaseCredentials;
import javasabr.mqtt.auth.api.DatabaseProperties;
import javasabr.mqtt.auth.credentials.source.config.DatabaseCredentialsSourceFactories;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "authentication.credentials-source.database.enabled", havingValue = "true")
@ConditionalOnClass(name = "javasabr.mqtt.auth.credentials.source.DatabaseCredentialsSource")
public class DatabaseCredentialsSourceSpringConfig {

  @Bean
  @DependsOn("flyway")
  CredentialsSource dbCredentialsSource(
      DatabaseProperties databaseCredentialsSourceProperties,
      DatabaseCredentials readerDatabaseCredentials) {
    return DatabaseCredentialsSourceFactories.databaseCredentialsSource()
        .databaseCredentialsSourceProperties(databaseCredentialsSourceProperties)
        .readerDatabaseCredentials(readerDatabaseCredentials)
        .build();
  }

  @Bean(initMethod = "migrate")
  Flyway flyway(
      DatabaseConnectionProperties databaseCredentialsSourceProperties,
      DatabaseCredentials adminDatabaseCredentials) {
    return DatabaseCredentialsSourceFactories.flyway()
        .databaseCredentialsSourceProperties(databaseCredentialsSourceProperties)
        .adminDatabaseCredentials(adminDatabaseCredentials)
        .build();
  }
}
