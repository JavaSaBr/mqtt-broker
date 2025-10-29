package javasabr.mqtt.model.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MalformedProtocolMqttException extends MqttException {
  public MalformedProtocolMqttException(String message) {
    super(message);
  }
}
