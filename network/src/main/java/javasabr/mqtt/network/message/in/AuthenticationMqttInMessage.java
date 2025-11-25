package javasabr.mqtt.network.message.in;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.AuthenticateReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Authentication exchange.
 */
@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationMqttInMessage extends MqttInMessage {

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

  // properties
  String reason;
  String authenticationMethod;

  byte[] authenticationData;

  public AuthenticationMqttInMessage(byte messageFlags) {
    super(messageFlags);
    this.reasonCode = AuthenticateReasonCode.SUCCESS;
    this.reason = StringUtils.EMPTY;
    this.authenticationMethod = StringUtils.EMPTY;
    this.authenticationData = ArrayUtils.EMPTY_BYTE_ARRAY;
  }

  @Override
  public byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901219
    reasonCode = AuthenticateReasonCode.of(readByteUnsigned(buffer));
  }

  @Override
  protected Set<MqttMessageProperty> availableProperties() {
    return AVAILABLE_PROPERTIES;
  }

  @Override
  protected void applyProperty(MqttMessageProperty property, byte[] value) {
    switch (property) {
      case AUTHENTICATION_DATA -> authenticationData = value;
      default -> unsupportedProperty(property);
    }
  }

  @Override
  protected void applyProperty(MqttMessageProperty property, String value) {
    switch (property) {
      case REASON_STRING -> reason = value;
      case AUTHENTICATION_METHOD -> authenticationMethod = value;
      default -> unsupportedProperty(property);
    }
  }
}
