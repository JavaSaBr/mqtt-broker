package javasabr.mqtt.network.message.in;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.AuthenticateReasonCode;
import javasabr.mqtt.network.MqttConnection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;

/**
 * Authentication exchange.
 */
@Getter
@Accessors
@FieldDefaults(level = AccessLevel.PROTECTED)
public class AuthenticationMqttInMessage extends MqttInMessage {

  public static final byte MESSAGE_FLAGS = 0b0000_0000;
  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.AUTHENTICATION.ordinal();

  static {
    DebugUtils.registerIncludedFields("reasonCode");
  }

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
  @Nullable
  String reason;
  @Nullable
  String authenticationMethod;

  byte @Nullable [] authenticationData;

  public AuthenticationMqttInMessage(byte messageFlags) {
    super(messageFlags);
    this.reasonCode = AuthenticateReasonCode.SUCCESS;
  }

  @Override
  public byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  public String name() {
    return MqttMessageType.AUTHENTICATION.name();
  }

  @Override
  protected boolean validMessageFlags(byte messageFlags) {
    return messageFlags == MESSAGE_FLAGS;
  }

  @Override
  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901219
    reasonCode = AuthenticateReasonCode.ofCode(readByteUnsigned(buffer));
  }

  @Override
  protected Set<MqttMessageProperty> availableProperties() {
    return AVAILABLE_PROPERTIES;
  }

  @Override
  protected void applyProperty(MqttMessageProperty property, byte[] value) {
    switch (property) {
      case AUTHENTICATION_DATA -> {
        if (authenticationData != null) {
          alreadyPresentedProperty(property);
        }
        authenticationData = value;
      }
      default -> unsupportedProperty(property);
    }
  }

  @Override
  protected void applyProperty(MqttMessageProperty property, String value) {
    switch (property) {
      case REASON_STRING -> {
        if (reason != null) {
          alreadyPresentedProperty(property);
        }
        reason = value;
      }
      case AUTHENTICATION_METHOD -> {
        if (authenticationMethod != null) {
          alreadyPresentedProperty(property);
        }
        authenticationMethod = value;
      }
      default -> unsupportedProperty(property);
    }
  }
}
