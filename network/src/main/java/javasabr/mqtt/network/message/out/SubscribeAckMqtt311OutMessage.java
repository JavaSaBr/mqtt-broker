package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Subscribe acknowledgement.
 */
@Getter
@Accessors
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubscribeAckMqtt311OutMessage extends TrackableMqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.SUBSCRIBE_ACK.ordinal();

  static {
    DebugUtils.registerIncludedFields("reasonCodes", "messageId");
  }

  /**
   * The order of Reason Codes in the SUBACK packet MUST match the order of Topic Filters in the SUBSCRIBE packet.
   */
  Array<SubscribeAckReasonCode> reasonCodes;

  public SubscribeAckMqtt311OutMessage(int messageId, Array<SubscribeAckReasonCode> reasonCodes) {
    super(messageId);
    this.reasonCodes = reasonCodes;
  }

  @Override
  public int expectedLength(MqttConnection connection) {
    return 2 + reasonCodes.size();
  }

  @Override
  protected byte messageTypeId() {
    return MESSAGE_TYPE;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.SUBSCRIBE_ACK;
  }

  @Override
  protected void writePayload(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718071
    for (var reasonCode : reasonCodes) {
      writeByte(buffer, reasonCode.code());
    }
  }
}
