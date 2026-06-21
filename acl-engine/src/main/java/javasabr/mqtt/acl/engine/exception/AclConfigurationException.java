package javasabr.mqtt.acl.engine.exception;

public class AclConfigurationException extends RuntimeException {

  public AclConfigurationException(String message) {
    super(message);
  }

  public AclConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}
