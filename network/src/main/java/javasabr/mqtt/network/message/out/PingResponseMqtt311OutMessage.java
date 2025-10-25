package javasabr.mqtt.network.message.out;

import javasabr.mqtt.network.message.MqttMessageType;

/**
 * PING response.
 */
public class PingResponseMqtt311OutMessage extends MqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.PING_RESPONSE.ordinal();

  @Override
  protected byte messageType() {
    return MESSAGE_TYPE;
  }
}
