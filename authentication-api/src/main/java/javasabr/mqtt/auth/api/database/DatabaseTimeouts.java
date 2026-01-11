package javasabr.mqtt.auth.api.database;

import javasabr.mqtt.auth.api.exception.AuthenticationConfigException;

public record DatabaseTimeouts(String lockTimeout, String statementTimeout) {

  public DatabaseTimeouts(int lockTimeoutSeconds, int statementTimeoutSeconds) {
    if (lockTimeoutSeconds <= 0) {
      throw new AuthenticationConfigException("Database lock timeout '%s' is not valid".formatted(
          lockTimeoutSeconds));
    }
    if (statementTimeoutSeconds <= 0) {
      throw new AuthenticationConfigException("Database statement timeout '%s' is not valid".formatted(
          statementTimeoutSeconds));
    }
    this("%ss".formatted(lockTimeoutSeconds), "%ss".formatted(statementTimeoutSeconds));
  }
}
