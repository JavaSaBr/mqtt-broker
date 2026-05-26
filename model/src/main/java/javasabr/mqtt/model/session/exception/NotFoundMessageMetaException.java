package javasabr.mqtt.model.session.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotFoundMessageMetaException extends MessageMetaException {
  int messageId;

  public NotFoundMessageMetaException(int messageId, String message) {
    super(message);
    this.messageId = messageId;
  }
}
