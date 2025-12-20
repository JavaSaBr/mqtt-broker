package javasabr.mqtt.broker.application.config;

import static io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider.OPTIONS;
import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static io.r2dbc.spi.ConnectionFactoryOptions.HOST;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.PORT;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import java.util.Map;
import javasabr.mqtt.model.DatabaseUrlBuilder;
import org.flywaydb.core.Flyway;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DatabaseConnectionProperties.class)
public class DatabaseSpringConfig {

  @Bean
  @DependsOn("flyway")
  ConnectionFactory connectionFactory(DatabaseConnectionProperties config) {
    javasabr.mqtt.model.Credentials reader = config.users().get("reader");
    Map<String, String> timeoutOptions = Map.of(
        "lock_timeout", config.lockTimeout(),
        "statement_timeout", config.statementTimeout());
    ConnectionFactoryOptions connectionFactoryOptions = ConnectionFactoryOptions.builder()
        .option(DRIVER, config.driver())
        .option(HOST, config.host())
        .option(PORT, config.port())
        .option(USER, reader.username())
        .option(PASSWORD, reader.password())
        .option(DATABASE, config.name())
        .option(OPTIONS, timeoutOptions).build();
    ConnectionFactory connectionFactory = ConnectionFactories.get(connectionFactoryOptions);
    ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(connectionFactory)
        .maxIdleTime(config.maxIdleTime())
        .maxSize(config.maxPoolSize())
        .initialSize(config.initialPoolSize())
        .build();
    return new ConnectionPool(configuration);
  }

  @Bean(initMethod = "migrate")
  Flyway flyway(DatabaseUrlBuilder databaseUrlBuilder, DatabaseConnectionProperties dbProperties) {
    javasabr.mqtt.model.Credentials admin = dbProperties.users().get("admin");
    return Flyway.configure()
        .dataSource(databaseUrlBuilder.build(dbProperties), admin.username(), admin.password())
        .load();
  }

  @Bean
  DatabaseUrlBuilder databaseUrlBuilder() {
    return dbProps -> "jdbc:%s://%s:%s/%s".formatted(dbProps.driver(), dbProps.host(), dbProps.port(), dbProps.name());
  }
}
