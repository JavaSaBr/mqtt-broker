package javasabr.mqtt.network.message.in;

import javasabr.mqtt.network.message.MqttMessageType;

/**
 * PING request.
 */
public class PingRequestMqttInMessage extends MqttInMessage {

  public static final byte MESSAGE_TYPE = (byte) MqttMessageType.PING_REQUEST.ordinal();

  public PingRequestMqttInMessage(byte info) {
    super(info);
  }

  @Override
  public byte messageType() {
    return MESSAGE_TYPE;
  }
}
