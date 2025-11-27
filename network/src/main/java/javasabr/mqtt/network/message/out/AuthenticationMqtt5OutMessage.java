package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.AuthenticateReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

/**
 * Authentication exchange.
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationMqtt5OutMessage extends MqttOutMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.AUTHENTICATION.ordinal();

  private static final Set<MqttMessageProperty> AVAILABLE_PROPERTIES = EnumSet.of(
      /*
        Followed by a UTF-8 Encoded String containing the name of the authentication method. It is a Protocol
        Error to omit the Authentication Method or to include it more than once. Refer to section 4.12 for more
        information about extended authentication.
       */
      MqttMessageProperty.AUTHENTICATION_METHOD,
      /*
        Followed by Binary Data containing authentication data. It is a Protocol Error to include Authentication
        Data more than once. The contents of this data are defined by the authentication method. Refer to
        section 4.12 for more information about extended authentication.
       */
      MqttMessageProperty.AUTHENTICATION_DATA,
      /*
        Followed by the UTF-8 Encoded String representing the reason for the disconnect. This Reason String is
        human readable, designed for diagnostics and SHOULD NOT be parsed by the receiver.

        The sender MUST NOT send this property if it would increase the size of the AUTH packet beyond the
        Maximum Packet Size specified by the receiver [MQTT-3.15.2-2]. It is a Protocol Error to include the
        Reason String more than once.
       */
      MqttMessageProperty.REASON_STRING,
      /*
        Followed by UTF-8 String Pair. This property may be used to provide additional diagnostic or other
        information. The sender MUST NOT send this property if it would increase the size of the AUTH packet
        beyond the Maximum Packet Size specified by the receiver [MQTT-3.15.2-3]. The User Property is
        allowed to appear multiple times to represent multiple name, value pairs. The same name is allowed to
        appear more than once.
       */
      MqttMessageProperty.USER_PROPERTY);

  AuthenticateReasonCode reasonCode;

  @Nullable
  String reason;
  @Nullable
  String authenticateMethod;

  byte @Nullable [] authenticateData;

  Array<StringPair> userProperties;

  @Override
  protected byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  protected void writeVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901219
    writeByte(buffer, reasonCode.code());
  }

  @Override
  protected boolean isPropertiesSupported(MqttConnection connection) {
    return true;
  }

  @Override
  protected void writeProperties(MqttConnection connection, ByteBuffer buffer) {
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901221
    writeStringPairProperties(buffer, MqttMessageProperty.USER_PROPERTY, userProperties);
    writeNotEmptyProperty(buffer, MqttMessageProperty.REASON_STRING, reason);
    writeNotEmptyProperty(buffer, MqttMessageProperty.AUTHENTICATION_METHOD, authenticateMethod);
    writeNotEmptyProperty(buffer, MqttMessageProperty.AUTHENTICATION_DATA, authenticateData);
  }
}
