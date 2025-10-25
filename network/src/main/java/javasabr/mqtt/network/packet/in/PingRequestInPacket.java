package javasabr.mqtt.network.packet.in;

import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.MqttInMessage;

/**
 * PING request.
 */
public class PingRequestInPacket extends MqttInMessage {

  public static final byte PACKET_TYPE = (byte) MqttMessageType.PING_REQUEST.ordinal();

  public PingRequestInPacket(byte info) {
    super(info);
  }

  @Override
  public byte messageType() {
    return PACKET_TYPE;
  }
}
