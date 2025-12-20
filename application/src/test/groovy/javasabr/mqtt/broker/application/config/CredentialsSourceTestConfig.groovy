package javasabr.mqtt.broker.application.config

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Primary

import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD
import static io.r2dbc.spi.ConnectionFactoryOptions.PROTOCOL
import static io.r2dbc.spi.ConnectionFactoryOptions.USER
import static io.r2dbc.spi.ConnectionFactoryOptions.builder
import static DatabaseSpringConfig.DatabaseUrlBuilder

@Configuration
class CredentialsSourceTestConfig {

  @Bean
  @Primary
  @DependsOn("flyway")
  ConnectionFactory connectionFactory(DatabaseCredentialReaderProperties readerProperties) {
    ConnectionFactoryOptions options = builder()
        .option(DRIVER, "h2")
        .option(PROTOCOL, "mem")
        .option(DATABASE, "testdb")
        .option(USER, readerProperties.username())
        .option(PASSWORD, readerProperties.password())
        .option(Option.valueOf("DB_CLOSE_DELAY"), "-1")
        .build()
    return ConnectionFactories.get(options)
  }

  @Bean
  @Primary
  DatabaseUrlBuilder databaseUrlBuilder() {
    return { dbProps -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1" }
  }
}
