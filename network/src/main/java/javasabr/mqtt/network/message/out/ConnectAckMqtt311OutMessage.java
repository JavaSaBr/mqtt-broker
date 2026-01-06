package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Connect acknowledgment.
 */
@Getter
@Accessors
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public class ConnectAckMqtt311OutMessage extends MqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.CONNECT_ACK.ordinal();

  static {
    DebugUtils.registerIncludedFields("reasonCode", "sessionPresent");
  }

  /**
   * The values the Connect Reason Code are shown below. If a well formed CONNECT packet is received by the Server, but
   * the Server is unable to complete the Connection the Server MAY send a CONNACK packet containing the appropriate
   * Connect Reason code from this table. If a Server sends a CONNACK packet containing a Reason code of 128 or greater
   * it MUST then close the Network Connection
   */
  ConnectAckReasonCode reasonCode;

  /**
   * The Session Present flag informs the Client whether the Server is using Session State from a previous connection
   * for this ClientID. This allows the Client and Server to have a consistent view of the Session State. If the Server
   * accepts a connection with Clean Start set to 1, the Server MUST set Session Present to 0 in the CONNACK packet in
   * addition to setting a 0x00 (Success) Reason Code in the CONNACK packet
   */
  boolean sessionPresent;

  @Override
  protected byte messageTypeId() {
    return MESSAGE_TYPE;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.CONNECT_ACK;
  }

  @Override
  public int expectedLength(MqttConnection connection) {
    return PACKET_ID_SIZE;
  }

  @Override
  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718035
    buffer.put((byte) (sessionPresent ? 0x01 : 0x00));
    buffer.put(reasonCodeValue());
  }

  protected byte reasonCodeValue() {
    return (byte) reasonCode.mqtt311();
  }
}
