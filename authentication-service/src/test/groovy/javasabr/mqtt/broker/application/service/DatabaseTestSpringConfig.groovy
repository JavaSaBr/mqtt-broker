//file:noinspection GrMethodMayBeStatic
package javasabr.mqtt.broker.application.service

import javasabr.mqtt.auth.api.database.DatabaseCredentials
import javasabr.mqtt.auth.service.config.AuthenticationServiceSpringConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistrar
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@Configuration
@Import(AuthenticationServiceSpringConfig.class)
class DatabaseTestSpringConfig {

  private static final String POSTGRES_VERSION = "18.1"

  @Bean
  DatabaseCredentials readerDatabaseCredentials(PostgreSQLContainer _) {
    return new DatabaseCredentials("user", "pass")
  }

  @Bean
  DatabaseCredentials adminDatabaseCredentials(PostgreSQLContainer _) {
    return new DatabaseCredentials("user", "pass")
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  PostgreSQLContainer postgresSQLContainer() {
    return new PostgreSQLContainer(DockerImageName
        .parse(PostgreSQLContainer.IMAGE)
        .withTag(POSTGRES_VERSION))
        .withDatabaseName("testdb")
        .withUsername("user")
        .withPassword("pass")
  }

  @Bean
  DynamicPropertyRegistrar configurePostgresSQLContainerPort(PostgreSQLContainer container) {
    return { registry ->
      registry.add(
          "authentication.credentials-source.database.port",
          () -> container.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT))
    }
  }
}
