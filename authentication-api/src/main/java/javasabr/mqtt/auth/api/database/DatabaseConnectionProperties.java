package javasabr.mqtt.auth.api.database;

import javasabr.mqtt.base.util.PropertyAssert;

public record DatabaseConnectionProperties(DatabaseDriver driver, String host, int port, String dbName) {

  public DatabaseConnectionProperties(String driver, String host, int port, String dbName) {
    PropertyAssert.notEmpty(host, "Database host");
    PropertyAssert.positive(port, "Database port");
    PropertyAssert.notEmpty(dbName, "Database name");
    PropertyAssert.notEmpty(driver, "Database driver");
    DatabaseDriver databaseDriver = DatabaseDriver.BY_ALIAS.resolve(driver);
    PropertyAssert.notNull(databaseDriver, "Database driver '%s' is not supported", driver);

    this(databaseDriver, host, port, dbName);
  }
}
