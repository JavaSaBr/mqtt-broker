package javasabr.mqtt.network.message.out;

import javasabr.mqtt.model.message.MqttMessageType;

/**
 * PING response.
 */
public class PingResponseMqtt311OutMessage extends MqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.PING_RESPONSE.ordinal();

  @Override
  protected byte messageTypeId() {
    return MESSAGE_TYPE;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PING_RESPONSE;
  }
}
