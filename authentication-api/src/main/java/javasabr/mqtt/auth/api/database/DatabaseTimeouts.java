package javasabr.mqtt.auth.api.database;

import static javasabr.mqtt.auth.api.database.DatabasePoolProperties.assertPositive;

public record DatabaseTimeouts(String lockTimeout, String statementTimeout) {

  public DatabaseTimeouts(int lockTimeoutSeconds, int statementTimeoutSeconds) {
    assertPositive(lockTimeoutSeconds, "Database lock timeout '%s' is not valid");
    assertPositive(statementTimeoutSeconds, "Database statement timeout '%s' is not valid");

    this("%ss".formatted(lockTimeoutSeconds), "%ss".formatted(statementTimeoutSeconds));
  }
}
