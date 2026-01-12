package javasabr.mqtt.auth.api.database;

public record DatabaseConnectionProperties(DatabaseDriver driver, String host, int port, String dbName) {

  public DatabaseConnectionProperties(String driver, String host, int port, String dbName) {
    DatabaseDriver databaseDriver = DatabaseDriver.fromValue(driver);
    PropertyAssert.notNull(databaseDriver, "Database driver '%s' is not supported".formatted(driver));
    PropertyAssert.notEmpty(host, "Database host is not specified");
    PropertyAssert.positive(port, "Database port '%s' is not valid");
    PropertyAssert.notEmpty(dbName, "Database name is not specified");
    PropertyAssert.notEmpty(driver, "Database driver is not specified");
    this(databaseDriver, host, port, dbName);
  }
}
