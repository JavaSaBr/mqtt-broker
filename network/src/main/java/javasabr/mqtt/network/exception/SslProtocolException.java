package javasabr.mqtt.network.exception;

public class SslProtocolException extends RuntimeException {
  public SslProtocolException(String message) {
    super(message);
  }

  public SslProtocolException(String message, Throwable cause) {
    super(message, cause);
  }
}
