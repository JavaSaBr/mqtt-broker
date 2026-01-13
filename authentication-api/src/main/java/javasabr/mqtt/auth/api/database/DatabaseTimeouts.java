package javasabr.mqtt.auth.api.database;

import javasabr.mqtt.base.util.PropertyAssert;

public record DatabaseTimeouts(String lockTimeout, String statementTimeout) {

  public DatabaseTimeouts(int lockTimeoutSeconds, int statementTimeoutSeconds) {
    PropertyAssert.positive(lockTimeoutSeconds, "Database lock timeout '%s' is not valid");
    PropertyAssert.positive(statementTimeoutSeconds, "Database statement timeout '%s' is not valid");

    this("%ss".formatted(lockTimeoutSeconds), "%ss".formatted(statementTimeoutSeconds));
  }
}
