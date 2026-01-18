package javasabr.mqtt.auth.credentials.source;

import java.util.List;
import javasabr.mqtt.auth.api.database.DatabaseConnectionProperties;
import javasabr.mqtt.auth.api.database.DatabaseCredentials;
import lombok.Builder;
import org.flywaydb.core.Flyway;

public class FlywayBuilder {

  @SuppressWarnings("unused")
  @Builder(builderMethodName = "create", builderClassName = "InnerBuilder")
  private static Flyway createFlyway(
      DatabaseConnectionProperties databaseConnectionProperties,
      DatabaseCredentials adminDatabaseCredentials,
      String[] databaseMigrationLocations) {
    String databaseUrl = "jdbc:%s://%s:%s/%s".formatted(
        databaseConnectionProperties.driver().value(),
        databaseConnectionProperties.host(),
        databaseConnectionProperties.port(),
        databaseConnectionProperties.dbName());
    return Flyway.configure()
        .dataSource(databaseUrl, adminDatabaseCredentials.username(), adminDatabaseCredentials.password())
        .locations(databaseMigrationLocations)
        .load();
  }
}
