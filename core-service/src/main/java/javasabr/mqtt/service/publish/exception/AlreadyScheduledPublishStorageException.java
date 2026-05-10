package javasabr.mqtt.service.publish.exception;

public class AlreadyScheduledPublishStorageException extends PublishStorageException {
  public AlreadyScheduledPublishStorageException(String message) {
    super(message);
  }
}
