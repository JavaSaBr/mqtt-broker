package javasabr.mqtt.auth.service.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import javasabr.mqtt.auth.api.CredentialsSource;
import javasabr.mqtt.auth.api.database.DatabaseConnectionProperties;
import javasabr.mqtt.auth.api.database.DatabaseCredentials;
import javasabr.mqtt.auth.api.database.DatabaseDriver;
import javasabr.mqtt.auth.api.database.DatabasePoolProperties;
import javasabr.mqtt.auth.api.database.DatabaseTimeoutsProperties;
import javasabr.mqtt.auth.credentials.source.config.DatabaseCredentialsSourceFactories;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.convert.ConversionService;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "authentication.credentials-source.database.enabled", havingValue = "true")
@ConditionalOnClass(name = "javasabr.mqtt.auth.credentials.source.DatabaseCredentialsSource")
public class DatabaseCredentialsSourceSpringConfig {

  @Bean
  public ConversionService conversionService() {
    return new ApplicationConversionService();
  }

  @Bean
  DatabasePoolProperties databasePoolPropertiesRecord(
      @Value("${authentication.credentials-source.database.pool.max-idle-time}") @DurationUnit(ChronoUnit.MINUTES) Duration maxIdleTime,
      @Value("${authentication.credentials-source.database.pool.initial-size}") int initialSize,
      @Value("${authentication.credentials-source.database.pool.max-size}") int maxSize){
    return new DatabasePoolProperties(maxIdleTime, initialSize, maxSize);
  }

  @Bean
  DatabaseConnectionProperties databaseConnectionPropertiesRecord(
      @Value("${authentication.credentials-source.database.driver}") DatabaseDriver driver,
      @Value("${authentication.credentials-source.database.host}") String host,
      @Value("${authentication.credentials-source.database.port}") int port,
      @Value("${authentication.credentials-source.database.name}") String name){
    return new DatabaseConnectionProperties(driver, host, port, name);
  }

  @Bean
  DatabaseTimeoutsProperties databaseTimeoutsPropertiesRecord(
      @Value("${authentication.credentials-source.database.timeout.lock-timeout}") String lockTimeout,
      @Value("${authentication.credentials-source.database.timeout.statement-timeout}") String statementTimeout){
    return new DatabaseTimeoutsProperties(lockTimeout, statementTimeout);
  }

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
      DatabaseConnectionProperties databaseCredentialsSourceProperties,
      DatabaseCredentials adminDatabaseCredentials) {
    return DatabaseCredentialsSourceFactories.flyway()
        .databaseCredentialsSourceProperties(databaseCredentialsSourceProperties)
        .adminDatabaseCredentials(adminDatabaseCredentials)
        .build();
  }
}
