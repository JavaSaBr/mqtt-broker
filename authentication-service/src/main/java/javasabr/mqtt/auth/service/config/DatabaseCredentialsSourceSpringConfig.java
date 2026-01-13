package javasabr.mqtt.auth.service.config;

import javasabr.mqtt.auth.api.CredentialsSource;
import javasabr.mqtt.auth.api.database.DatabaseConnectionProperties;
import javasabr.mqtt.auth.api.database.DatabaseCredentials;
import javasabr.mqtt.auth.api.database.DatabasePoolProperties;
import javasabr.mqtt.auth.api.database.DatabaseTimeoutProperties;
import javasabr.mqtt.auth.credentials.source.DatabaseCredentialsSource;
import javasabr.mqtt.auth.credentials.source.FlywayBuilder;
import lombok.CustomLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@CustomLog
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "authentication.credentials-source.database.enabled", havingValue = "true")
@ConditionalOnClass(name = "javasabr.mqtt.auth.credentials.source.DatabaseCredentialsSource")
public class DatabaseCredentialsSourceSpringConfig {

  @Bean
  DatabasePoolProperties credentialsSourceDatabasePoolProperties(
      @Value("${authentication.credentials-source.database.pool.max-idle-time-seconds}") int maxIdleTimeSeconds,
      @Value("${authentication.credentials-source.database.pool.initial-size}") int initialSize,
      @Value("${authentication.credentials-source.database.pool.max-size}") int maxSize) {
    log.info("Initializing CredentialsSourceDatabasePoolProperties...");
    return new DatabasePoolProperties(maxIdleTimeSeconds, initialSize, maxSize);
  }

  @Bean
  DatabaseConnectionProperties credentialsSourceDatabaseConnectionProperties(
      @Value("${authentication.credentials-source.database.driver}") String driver,
      @Value("${authentication.credentials-source.database.host}") String host,
      @Value("${authentication.credentials-source.database.port}") int port,
      @Value("${authentication.credentials-source.database.name}") String name) {
    log.info("Initializing CredentialsSourceDatabaseConnectionProperties...");
    return new DatabaseConnectionProperties(driver, host, port, name);
  }

  @Bean
  DatabaseTimeoutProperties credentialsSourceDatabaseTimeoutsProperties(
      @Value("${authentication.credentials-source.database.timeout.lock-timeout-seconds}") int lockTimeoutSeconds,
      @Value("${authentication.credentials-source.database.timeout.statement-timeout-seconds}")
      int statementTimeoutSeconds) {
    log.info("Initializing CredentialsSourceDatabaseTimeoutsProperties...");
    return new DatabaseTimeoutProperties(lockTimeoutSeconds, statementTimeoutSeconds);
  }

  @Bean
  @DependsOn("credentialsSourceFlyway")
  CredentialsSource databaseCredentialsSource(
      DatabasePoolProperties databasePoolProperties,
      DatabaseTimeoutProperties databaseTimeoutsProperties,
      DatabaseConnectionProperties databaseConnectionProperties,
      DatabaseCredentials readerDatabaseCredentials) {
    log.info("Initializing DatabaseCredentialsSource...");
    return DatabaseCredentialsSource.builder()
        .databasePoolProperties(databasePoolProperties)
        .databaseTimeoutsProperties(databaseTimeoutsProperties)
        .databaseConnectionProperties(databaseConnectionProperties)
        .readerDatabaseCredentials(readerDatabaseCredentials)
        .build();
  }

  @Bean(initMethod = "migrate")
  org.flywaydb.core.Flyway credentialsSourceFlyway(
      DatabaseConnectionProperties credentialsSourceDatabaseConnectionProperties,
      DatabaseCredentials adminDatabaseCredentials) {
    log.info("Initializing CredentialsSourceFlyway...");
    return FlywayBuilder.create()
        .databaseConnectionProperties(credentialsSourceDatabaseConnectionProperties)
        .adminDatabaseCredentials(adminDatabaseCredentials)
        .build();
  }
}
