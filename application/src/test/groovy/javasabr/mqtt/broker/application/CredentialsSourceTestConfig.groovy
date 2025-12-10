package javasabr.mqtt.broker.application

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import javasabr.mqtt.broker.application.config.CredentialsSourceDatabaseConfig
import javasabr.mqtt.service.auth.source.CredentialSource
import javasabr.mqtt.service.auth.source.R2dbcCredentialsSource
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator

class CredentialsSourceTestConfig {

  @Bean
  CredentialSource credentialSource(
      ConnectionFactory connectionFactory,
      CredentialsSourceDatabaseConfig credentialsSourceDatabaseConfig) {
    return new R2dbcCredentialsSource(connectionFactory, credentialsSourceDatabaseConfig.credentialsQuery())
  }

  @Bean
  ConnectionFactory connectionFactory() {
    return ConnectionFactories.get("r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1")
  }

  @Bean
  ConnectionFactoryInitializer datasourceInitializer(ConnectionFactory connectionFactory) {
    ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer()
    initializer.setConnectionFactory(connectionFactory)
    ResourceDatabasePopulator populator = new ResourceDatabasePopulator()
    populator.addScript(new ClassPathResource("auth/user-credentials-schema.sql"))
    populator.addScript(new ClassPathResource("auth/user-credentials-data.sql"))
    initializer.setDatabasePopulator(populator)
    return initializer
  }
}
