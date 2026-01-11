package javasabr.mqtt.auth.api.database;

import static javasabr.mqtt.auth.api.database.DatabasePoolProperties.assertPositive;

import javasabr.mqtt.auth.api.exception.AuthenticationConfigException;
import javasabr.rlib.common.util.StringUtils;

public record DatabaseConnectionProperties(DatabaseDriver driver, String host, int port, String dbName) {

  public DatabaseConnectionProperties(String driver, String host, int port, String dbName) {
    assertSpecified(host, "Database host is not specified");
    assertPositive(port, "Database port '%s' is not valid");
    assertSpecified(dbName, "Database name is not specified");
    assertSpecified(driver, "Database driver is not specified");
    DatabaseDriver databaseDriver = DatabaseDriver.fromValue(driver);
    if (databaseDriver == null) {
      throw new AuthenticationConfigException("Database driver '%s' is not supported".formatted(driver));
    }
    this(databaseDriver, host, port, dbName);
  }

  public static void assertSpecified(String value, String message) {
    if (StringUtils.isEmpty(value)) {
      throw new AuthenticationConfigException(message);
    }
  }
}
