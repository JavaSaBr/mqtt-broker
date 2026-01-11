package javasabr.mqtt.auth.api.database;

import javasabr.mqtt.auth.api.exception.AuthenticationConfigException;
import javasabr.rlib.common.util.StringUtils;

public record DatabaseConnectionProperties(DatabaseDriver driver, String host, int port, String dbName) {

  public DatabaseConnectionProperties(String driver, String host, int port, String dbName) {
    if (StringUtils.isEmpty(host)) {
      throw new AuthenticationConfigException("Database host is not specified");
    }
    if (port <= 0) {
      throw new AuthenticationConfigException("Database port '%s' is not valid".formatted(port));
    }
    if (StringUtils.isEmpty(dbName)) {
      throw new AuthenticationConfigException("Database name is not specified");
    }
    if (StringUtils.isEmpty(driver)) {
      throw new AuthenticationConfigException("Database driver is not specified");
    }
    DatabaseDriver databaseDriver = DatabaseDriver.fromValue(driver);
    if (databaseDriver == null) {
      throw new AuthenticationConfigException("Database driver '%s' is not supported".formatted(driver));
    }
    this(databaseDriver, host, port, dbName);
  }
}
