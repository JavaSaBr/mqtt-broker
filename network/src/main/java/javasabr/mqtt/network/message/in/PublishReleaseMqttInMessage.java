package javasabr.mqtt.network.message.in;

import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode;

/**
 * Publish release (QoS 2 delivery part 2).
 */
public class PublishReleaseMqttInMessage extends PublishControlMqttInMessage<PublishReleaseReasonCode>
    implements TrackableMqttMessage {

  public static final byte MESSAGE_FLAGS = 0b0000_0010;
  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.PUBLISH_RELEASE.ordinal();

  private static final Set<MqttMessageProperty> AVAILABLE_PROPERTIES = EnumSet.of(
      /*
        Followed by the UTF-8 Encoded String representing the reason associated with this response. This
        Reason String is human readable, designed for diagnostics and SHOULD NOT be parsed by the
        receiver.

        The sender uses this value to give additional information to the receiver. The sender MUST NOT send
        this Property if it would increase the size of the PUBREL packet beyond the Maximum Packet Size
        specified by the receiver [MQTT-3.6.2-2]. It is a Protocol Error to include the Reason String more than
        once.
       */
      MqttMessageProperty.REASON_STRING,
      /*
        Followed by UTF-8 String Pair. This property can be used to provide additional diagnostic or other
        information for the PUBREL. The sender MUST NOT send this property if it would increase the size of the
        PUBREL packet beyond the Maximum Packet Size specified by the receiver [MQTT-3.6.2-3]. The User
        Property is allowed to appear multiple times to represent multiple name, value pairs. The same name is
        allowed to appear more than once
       */
      MqttMessageProperty.USER_PROPERTY);

  public PublishReleaseMqttInMessage(byte messageFlags) {
    super(messageFlags);
  }

  @Override
  public byte messageTypeId() {
    return MESSAGE_TYPE;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_RELEASE;
  }
  
  @Override
  protected boolean validMessageFlags(byte messageFlags) {
    return messageFlags == MESSAGE_FLAGS;
  }

  @Override
  protected PublishReleaseReasonCode defaultReasonCode() {
    return PublishReleaseReasonCode.SUCCESS;
  }

  @Override
  protected PublishReleaseReasonCode readReasonCode(int unsignedByte) {
    return PublishReleaseReasonCode.ofCode(unsignedByte);
  }

  @Override
  protected Set<MqttMessageProperty> availableProperties() {
    return AVAILABLE_PROPERTIES;
  }

  public static PublishReleaseMqttInMessage of(int messageId, PublishReleaseReasonCode reasonCode) {
    var message = new PublishReleaseMqttInMessage(MESSAGE_FLAGS);
    message.messageId = messageId;
    message.reasonCode = reasonCode;
    return message;
  }
}
