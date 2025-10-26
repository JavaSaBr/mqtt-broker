package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Subscribe acknowledgement.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubscribeAckMqtt311OutMessage extends MqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.SUBSCRIBE_ACK.ordinal();

  static {
    DebugUtils.registerIncludedFields("reasonCodes", "messageId");
  }

  /**
   * The order of Reason Codes in the SUBACK packet MUST match the order of Topic Filters in the SUBSCRIBE packet.
   */
  Array<SubscribeAckReasonCode> reasonCodes;

  /**
   * The Packet Identifier from the SUBSCRIBE.
   */
  int messageId;

  @Override
  public int expectedLength(MqttConnection connection) {
    return 2 + reasonCodes.size();
  }

  @Override
  protected byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718070
    writeShort(buffer, messageId);
  }

  @Override
  protected void writePayload(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718071
    for (var reasonCode : reasonCodes) {
      writeByte(buffer, reasonCode.getValue());
    }
  }
}
