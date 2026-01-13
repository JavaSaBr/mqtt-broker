package javasabr.mqtt.broker.application.service;

import javasabr.mqtt.auth.api.database.DatabaseCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Configuration
public class DatabaseTestSpringConfig {

  @Bean
  @DependsOn("postgreSQLContainer")
  public DatabaseCredentials readerDatabaseCredentials() {
    return new DatabaseCredentials("user", "");
  }

  @Bean
  @DependsOn("postgreSQLContainer")
  public DatabaseCredentials adminDatabaseCredentials() {
    return new DatabaseCredentials("user", "");
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  public PostgreSQLContainer postgreSQLContainer() {
    //noinspection resource
    return new PostgreSQLContainer("postgres:9.6.12")
        .withDatabaseName("testdb")
        .withUsername("user")
        .withPassword("");
  }

  @Bean
  public DynamicPropertyRegistrar configurePostgresSQLProperties(PostgreSQLContainer container) {
    return registry -> registry.add(
        "authentication.credentials-source.database.port",
        () -> container.getMappedPort(5432));
  }
}
