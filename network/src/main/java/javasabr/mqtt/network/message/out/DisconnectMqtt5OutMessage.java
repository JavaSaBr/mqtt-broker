package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

/**
 * Disconnect notification.
 */
@Getter
@Accessors
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DisconnectMqtt5OutMessage extends DisconnectMqtt311OutMessage {

  private static final Set<MqttMessageProperty> AVAILABLE_PROPERTIES = EnumSet.of(
      /*
        If the Session Expiry Interval is absent, the Session Expiry Interval in the CONNECT packet is used.

        The Session Expiry Interval MUST NOT be sent on a DISCONNECT by the Server [MQTT-3.14.2-2].

        If the Session Expiry Interval in the CONNECT packet was zero, then it is a Protocol Error to set a non
        zero Session Expiry Interval in the DISCONNECT packet sent by the Client. If such a non-zero Session
        Expiry Interval is received by the Server, it does not treat it as a valid DISCONNECT packet. The Server
        uses DISCONNECT with Reason Code 0x82 (Protocol Error) as described in
       */
      MqttMessageProperty.SESSION_EXPIRY_INTERVAL,
      /*
        The sender MUST NOT send this Property if it would increase the size of the DISCONNECT packet
        beyond the Maximum Packet Size specified by the receiver [MQTT-3.14.2-3]. It is a Protocol Error to
        include the Reason String more than once.
       */
      MqttMessageProperty.REASON_STRING,
      /*
        Followed by UTF-8 String Pair. This property may be used to provide additional diagnostic or other
        information. The sender MUST NOT send this property if it would increase the size of the DISCONNECT
        packet beyond the Maximum Packet Size specified by the receiver [MQTT-3.14.2-4]. The User Property is
        allowed to appear multiple times to represent multiple name, value pairs. The same name is allowed to
        appear more than once.
       */
      MqttMessageProperty.USER_PROPERTY,
      /*
        The Server sends DISCONNECT including a Server Reference and Reason Code {0x9C (Use another
        2601 server)} or 0x9D (Server moved) as described in section 4.13.
       */
      MqttMessageProperty.SERVER_REFERENCE);

  DisconnectReasonCode reasonCode;
  Array<StringPair> userProperties;

  @Nullable
  String reason;
  @Nullable
  String serverReference;

  long sessionExpiryInterval;

  @Override
  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901207
    writeByte(buffer, reasonCode.code());
  }

  @Override
  protected boolean isPropertiesSupported(MqttConnection connection) {
    return true;
  }

  @Override
  protected void writeProperties(MqttConnection connection, ByteBuffer buffer) {
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901209
    writeStringPairProperties(buffer, MqttMessageProperty.USER_PROPERTY, userProperties);
    writeNotEmptyProperty(buffer, MqttMessageProperty.REASON_STRING, reason);
    writeNotEmptyProperty(buffer, MqttMessageProperty.SERVER_REFERENCE, serverReference);
    if (sessionExpiryInterval != MqttProperties.SESSION_EXPIRY_INTERVAL_UNDEFINED) {
      writeProperty(
          buffer,
          MqttMessageProperty.SESSION_EXPIRY_INTERVAL,
          sessionExpiryInterval,
          MqttProperties.SESSION_EXPIRY_INTERVAL_DEFAULT);
    }
  }
}
