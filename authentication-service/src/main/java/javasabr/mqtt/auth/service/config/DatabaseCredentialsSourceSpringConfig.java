package javasabr.mqtt.auth.service.config;

import javasabr.mqtt.auth.api.CredentialsSource;
import javasabr.mqtt.auth.api.database.DatabaseConnectionProperties;
import javasabr.mqtt.auth.api.database.DatabaseCredentials;
import javasabr.mqtt.auth.api.database.DatabasePoolProperties;
import javasabr.mqtt.auth.api.database.DatabaseTimeouts;
import javasabr.mqtt.auth.credentials.source.config.DatabaseCredentialsSourceFactories;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
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
  DatabasePoolProperties databasePoolPropertiesRecord(
      @Value("${authentication.credentials-source.database.pool.max-idle-time-seconds}") int maxIdleTimeSeconds,
      @Value("${authentication.credentials-source.database.pool.initial-size}") int initialSize,
      @Value("${authentication.credentials-source.database.pool.max-size}") int maxSize) {
    return new DatabasePoolProperties(maxIdleTimeSeconds, initialSize, maxSize);
  }

  @Bean
  DatabaseConnectionProperties databaseConnectionPropertiesRecord(
      @Value("${authentication.credentials-source.database.driver}") String driver,
      @Value("${authentication.credentials-source.database.host}") String host,
      @Value("${authentication.credentials-source.database.port}") int port,
      @Value("${authentication.credentials-source.database.name}") String name) {
    return new DatabaseConnectionProperties(driver, host, port, name);
  }

  @Bean
  DatabaseTimeouts databaseTimeoutsPropertiesRecord(
      @Value("${authentication.credentials-source.database.timeout.lock-timeout-seconds}") int lockTimeoutSeconds,
      @Value("${authentication.credentials-source.database.timeout.statement-timeout-seconds}")
      int statementTimeoutSeconds) {
    return new DatabaseTimeouts(lockTimeoutSeconds, statementTimeoutSeconds);
  }

  @Bean
  @DependsOn("flyway")
  CredentialsSource dbCredentialsSource(
      DatabasePoolProperties databasePoolProperties,
      DatabaseTimeouts databaseTimeoutsProperties,
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
      DatabaseConnectionProperties databaseCredentialsSourceProperties,
      DatabaseCredentials adminDatabaseCredentials) {
    return DatabaseCredentialsSourceFactories.flyway()
        .databaseCredentialsSourceProperties(databaseCredentialsSourceProperties)
        .adminDatabaseCredentials(adminDatabaseCredentials)
        .build();
  }
}
