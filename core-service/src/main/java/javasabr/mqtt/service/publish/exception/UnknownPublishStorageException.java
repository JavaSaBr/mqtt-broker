package javasabr.mqtt.service.publish.exception;

import java.util.UUID;
import lombok.Getter;

@Getter
public class UnknownPublishStorageException extends PublishStorageException {
  private final UUID id;
  
  public UnknownPublishStorageException(String message, UUID id) {
    super(message);
    this.id = id;
  }
}
