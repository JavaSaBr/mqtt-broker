package javasabr.mqtt.auth.api.exception;

public class CredentialsSourceException extends RuntimeException {

  public CredentialsSourceException(String message) {
    super(message);
  }

  public CredentialsSourceException(String message, Throwable cause) {
    super(message, cause);
  }
}
