package javasabr.mqtt.network.message.out;

import javasabr.mqtt.model.message.MqttMessageType;

/**
 * PING request.
 */
public class PingRequestMqtt311OutMessage extends MqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.PING_REQUEST.ordinal();

  @Override
  protected byte messageTypeId() {
    return MESSAGE_TYPE;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PING_REQUEST;
  }
}
