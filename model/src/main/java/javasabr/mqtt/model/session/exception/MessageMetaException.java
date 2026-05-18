package javasabr.mqtt.model.session.exception;

public abstract class MessageMetaException extends RuntimeException {
  protected MessageMetaException(String message) {
    super(message);
  }
}
