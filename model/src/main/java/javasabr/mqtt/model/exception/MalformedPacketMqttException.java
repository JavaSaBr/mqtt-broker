package javasabr.mqtt.model.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MalformedPacketMqttException extends MqttException {

  public MalformedPacketMqttException(String message) {
    super(message);
  }
}
