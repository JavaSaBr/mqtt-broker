package javasabr.mqtt.network.packet.in;

import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.MqttInMessage;

/**
 * PING response.
 */
public class PingResponseInPacket extends MqttInMessage {

  public static final byte PACKET_TYPE = (byte) MqttMessageType.PING_RESPONSE.ordinal();

  public PingResponseInPacket(byte info) {
    super(info);
  }

  @Override
  public byte messageType() {
    return PACKET_TYPE;
  }
}
