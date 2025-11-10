package javasabr.mqtt.network.message.in;

import javasabr.mqtt.network.message.MqttMessageType;

/**
 * PING response.
 */
public class PingResponseMqttInMessage extends MqttInMessage {

  public static final byte MESSAGE_TYPE = (byte) MqttMessageType.PING_RESPONSE.ordinal();

  public PingResponseMqttInMessage(byte messageFlags) {
    super(messageFlags);
  }

  @Override
  public byte messageType() {
    return MESSAGE_TYPE;
  }
}
