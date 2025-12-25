//file:noinspection SpringJavaInjectionPointsAutowiringInspection
package javasabr.mqtt.broker.application.service

import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import javasabr.mqtt.auth.service.config.Credentials
import javasabr.mqtt.auth.service.config.DatabaseUrlBuilder
import javasabr.mqtt.auth.service.config.annotation.ConditionalOnDatabaseCredentialsSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD
import static io.r2dbc.spi.ConnectionFactoryOptions.PROTOCOL
import static io.r2dbc.spi.ConnectionFactoryOptions.USER

@Configuration
@ConditionalOnDatabaseCredentialsSource
class DatabaseTestSpringConfig {

  @Bean
  ConnectionFactoryOptions connectionFactoryOptions(@Qualifier("readerCredentials") Credentials credentials) {
    return ConnectionFactoryOptions.builder()
        .option(DRIVER, "h2")
        .option(PROTOCOL, "mem")
        .option(DATABASE, "testdb")
        .option(USER, credentials.username())
        .option(PASSWORD, credentials.password())
        .option(Option.valueOf("DB_CLOSE_DELAY"), "-1")
        .build()
  }

  @Bean
  DatabaseUrlBuilder databaseUrlBuilder() {
    return { "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1" }
  }
}
