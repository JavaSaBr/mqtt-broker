package javasabr.mqtt.network.message.in;

import javasabr.mqtt.model.message.MqttMessageType;

/**
 * PING request.
 */
public class PingRequestMqttInMessage extends MqttInMessage {

  public static final byte MESSAGE_FLAGS = 0b0000_0000;
  public static final byte MESSAGE_TYPE = (byte) MqttMessageType.PING_REQUEST.ordinal();

  public PingRequestMqttInMessage(byte messageFlags) {
    super(messageFlags);
  }

  @Override
  public byte messageTypeId() {
    return MESSAGE_TYPE;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PING_REQUEST;
  }

  @Override
  protected boolean validMessageFlags(byte messageFlags) {
    return messageFlags == MESSAGE_FLAGS;
  }
}
