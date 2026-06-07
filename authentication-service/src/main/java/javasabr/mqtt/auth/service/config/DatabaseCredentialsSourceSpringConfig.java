package javasabr.mqtt.auth.service.config;

import javasabr.mqtt.auth.api.CredentialsSource;
import javasabr.mqtt.auth.api.database.DatabaseConnectionProperties;
import javasabr.mqtt.auth.api.database.DatabaseCredentials;
import javasabr.mqtt.auth.api.database.DatabasePoolProperties;
import javasabr.mqtt.auth.api.database.DatabaseTimeoutProperties;
import javasabr.mqtt.auth.credentials.source.DatabaseCredentialsSource;
import javasabr.mqtt.auth.credentials.source.FlywayBuilder;
import lombok.CustomLog;
import org.flywaydb.core.Flyway;
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
      DatabasePoolProperties credentialsSourceDatabasePoolProperties,
      DatabaseTimeoutProperties credentialsSourceDatabaseTimeoutsProperties,
      DatabaseConnectionProperties credentialsSourceDatabaseConnectionProperties,
      DatabaseCredentials readerDatabaseCredentials) {
    log.info("Initializing DatabaseCredentialsSource...");
    return DatabaseCredentialsSource.builder()
        .databasePoolProperties(credentialsSourceDatabasePoolProperties)
        .databaseTimeoutsProperties(credentialsSourceDatabaseTimeoutsProperties)
        .databaseConnectionProperties(credentialsSourceDatabaseConnectionProperties)
        .readerDatabaseCredentials(readerDatabaseCredentials)
        .build();
  }

  @Bean(initMethod = "migrate")
  Flyway credentialsSourceFlyway(
      DatabaseConnectionProperties credentialsSourceDatabaseConnectionProperties,
      DatabaseCredentials adminDatabaseCredentials,
      @Value("${authentication.credentials-source.database.migration.location:classpath:db/migration}")
      String databaseMigrationLocation) {
    log.info("Initializing CredentialsSourceFlyway...");
    return FlywayBuilder.create()
        .databaseConnectionProperties(credentialsSourceDatabaseConnectionProperties)
        .adminDatabaseCredentials(adminDatabaseCredentials)
        .databaseMigrationLocations(new String[]{databaseMigrationLocation})
        .build();
  }

  @Bean
  DatabaseCredentials readerDatabaseCredentials() {
    return new DatabaseCredentials("postgres", "mysecretpassword");
  }

  @Bean
  DatabaseCredentials adminDatabaseCredentials() {
    return new DatabaseCredentials("postgres", "mysecretpassword");
  }
}
