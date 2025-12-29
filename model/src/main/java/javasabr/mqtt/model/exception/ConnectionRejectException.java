package javasabr.mqtt.model.exception;

import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConnectionRejectException extends MqttException {

  ConnectAckReasonCode reasonCode;

  public ConnectionRejectException(ConnectAckReasonCode reasonCode) {
    this.reasonCode = reasonCode;
  }

  public ConnectionRejectException(Throwable cause, ConnectAckReasonCode reasonCode) {
    super(cause);
    this.reasonCode = reasonCode;
  }

  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }
}
