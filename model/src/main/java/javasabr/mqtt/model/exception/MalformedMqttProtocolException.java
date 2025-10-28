package javasabr.mqtt.model.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MalformedMqttProtocolException extends MqttException {
  public MalformedMqttProtocolException(String message) {
    super(message);
  }
}
