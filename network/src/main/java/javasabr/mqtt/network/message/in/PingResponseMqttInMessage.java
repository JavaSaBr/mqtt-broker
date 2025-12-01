package javasabr.mqtt.network.message.in;

import javasabr.mqtt.model.message.MqttMessageType;

/**
 * PING response.
 */
public class PingResponseMqttInMessage extends MqttInMessage {

  public static final byte MESSAGE_TYPE = (byte) MqttMessageType.PING_RESPONSE.ordinal();

  public PingResponseMqttInMessage(byte messageFlags) {
    super(messageFlags);
  }

  @Override
  public byte messageTypeId() {
    return MESSAGE_TYPE;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PING_RESPONSE;
  }
}
