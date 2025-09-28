package javasabr.mqtt.model.exception;

public class CredentialsSourceException extends RuntimeException {

  public CredentialsSourceException(String message) {
    super(message);
  }

  public CredentialsSourceException(Throwable cause) {
    super(cause);
  }
}
