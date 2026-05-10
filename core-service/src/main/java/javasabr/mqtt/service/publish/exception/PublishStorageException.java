package javasabr.mqtt.service.publish.exception;

public abstract class PublishStorageException extends RuntimeException {
  protected PublishStorageException(String message) {
    super(message);
  }
}
