package javasabr.mqtt.auth.service.config;

import javasabr.mqtt.auth.api.CredentialsSource;
import javasabr.mqtt.auth.api.DatabaseConnectionProperties;
import javasabr.mqtt.auth.api.DatabaseCredentials;
import javasabr.mqtt.auth.api.DatabasePoolProperties;
import javasabr.mqtt.auth.api.DatabaseTimeoutsProperties;
import javasabr.mqtt.auth.credentials.source.config.DatabaseCredentialsSourceFactories;
import javasabr.mqtt.auth.service.config.property.SpringDatabaseConnectionProperties;
import javasabr.mqtt.auth.service.config.property.SpringDatabasePoolProperties;
import javasabr.mqtt.auth.service.config.property.SpringDatabaseTimeoutsProperties;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "authentication.credentials-source.database.enabled", havingValue = "true")
@ConditionalOnClass(name = "javasabr.mqtt.auth.credentials.source.DatabaseCredentialsSource")
@EnableConfigurationProperties({
    SpringDatabasePoolProperties.class,
    SpringDatabaseTimeoutsProperties.class,
    SpringDatabaseConnectionProperties.class
})
public class DatabaseCredentialsSourceSpringConfig {

  @Bean
  @DependsOn("flyway")
  CredentialsSource dbCredentialsSource(
      DatabasePoolProperties databasePoolProperties,
      DatabaseTimeoutsProperties databaseTimeoutsProperties,
      DatabaseConnectionProperties databaseConnectionProperties ,
      DatabaseCredentials readerDatabaseCredentials) {
    return DatabaseCredentialsSourceFactories.databaseCredentialsSource()
        .databasePoolProperties(databasePoolProperties)
        .databaseTimeoutsProperties(databaseTimeoutsProperties)
        .databaseConnectionProperties(databaseConnectionProperties)
        .readerDatabaseCredentials(readerDatabaseCredentials)
        .build();
  }

  @Bean(initMethod = "migrate")
  Flyway flyway(
      SpringDatabaseConnectionProperties databaseCredentialsSourceProperties,
      DatabaseCredentials adminDatabaseCredentials) {
    return DatabaseCredentialsSourceFactories.flyway()
        .databaseCredentialsSourceProperties(databaseCredentialsSourceProperties)
        .adminDatabaseCredentials(adminDatabaseCredentials)
        .build();
  }
}
