package javasabr.mqtt.model.exception;

public class AclConfigurationException extends RuntimeException {

  public AclConfigurationException(String message) {
    super(message);
  }

  public AclConfigurationException(Throwable cause) {
    super(cause);
  }
}
