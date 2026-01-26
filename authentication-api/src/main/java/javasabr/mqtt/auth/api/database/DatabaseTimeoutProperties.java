package javasabr.mqtt.auth.api.database;

import javasabr.mqtt.base.util.PropertyAssert;

public record DatabaseTimeoutProperties(String lockTimeout, String statementTimeout) {

  public DatabaseTimeoutProperties(int lockTimeoutSeconds, int statementTimeoutSeconds) {
    PropertyAssert.positive(lockTimeoutSeconds, "Database lock timeout");
    PropertyAssert.positive(statementTimeoutSeconds, "Database statement timeout");

    this("%ss".formatted(lockTimeoutSeconds), "%ss".formatted(statementTimeoutSeconds));
  }
}
