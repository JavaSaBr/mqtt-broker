package javasabr.mqtt.auth.api;

public class CredentialsSourceException extends RuntimeException {

  public CredentialsSourceException(String message) {
    super(message);
  }

  public CredentialsSourceException(Throwable cause) {
    super(cause);
  }
}
