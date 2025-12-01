package javasabr.mqtt.network.message.in;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Disconnect notification.
 */
@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DisconnectMqttInMessage extends MqttInMessage {

  public static final byte MESSAGE_TYPE = (byte) MqttMessageType.DISCONNECT.ordinal();

  static {
    DebugUtils.registerIncludedFields("reasonCode");
  }

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

  // properties
  String reason;
  String serverReference;

  long sessionExpiryInterval;

  public DisconnectMqttInMessage(byte messageFlags) {
    super(messageFlags);
    this.reasonCode = DisconnectReasonCode.NORMAL_DISCONNECTION;
    this.reason = StringUtils.EMPTY;
    this.serverReference = StringUtils.EMPTY;
    this.sessionExpiryInterval = MqttProperties.SESSION_EXPIRY_INTERVAL_DEFAULT;
  }

  @Override
  public byte messageTypeId() {
    return MESSAGE_TYPE;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.DISCONNECT;
  }

  @Override
  protected void readImpl(MqttConnection connection, ByteBuffer buffer) {
    this.sessionExpiryInterval = connection
        .clientConnectionConfig()
        .sessionExpiryInterval();
    super.readImpl(connection, buffer);
  }

  @Override
  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901207
    if (connection.isSupported(MqttVersion.MQTT_5) && buffer.hasRemaining()) {
      reasonCode = DisconnectReasonCode.ofCode(readByteUnsigned(buffer));
    }
  }

  @Override
  protected boolean isPropertiesSupported(MqttConnection connection, ByteBuffer buffer) {
    return connection.isSupported(MqttVersion.MQTT_5) && buffer.hasRemaining();
  }

  @Override
  protected Set<MqttMessageProperty> availableProperties() {
    return AVAILABLE_PROPERTIES;
  }

  @Override
  protected void applyProperty(MqttMessageProperty property, long value) {
    switch (property) {
      case SESSION_EXPIRY_INTERVAL: {
        sessionExpiryInterval = value;
        break;
      }
      default: {
        unsupportedProperty(property);
      }
    }
  }

  @Override
  protected void applyProperty(MqttMessageProperty property, String value) {
    switch (property) {
      case REASON_STRING: {
        reason = value;
        break;
      }
      case SERVER_REFERENCE: {
        serverReference = value;
        break;
      }
      default: {
        unsupportedProperty(property);
      }
    }
  }
}
