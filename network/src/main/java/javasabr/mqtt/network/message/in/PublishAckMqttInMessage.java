package javasabr.mqtt.network.message.in;

import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.reason.code.PublishAckReasonCode;

/**
 * Publish acknowledgment (QoS 1).
 */
public class PublishAckMqttInMessage extends PublishControlMqttInMessage<PublishAckReasonCode>
    implements TrackableMqttMessage {

  private static final int MESSAGE_TYPE = MqttMessageType.PUBLISH_ACK.ordinal();

  private static final Set<MqttMessageProperty> AVAILABLE_PROPERTIES = EnumSet.of(
      /*
        Followed by the UTF-8 Encoded String representing the reason associated with this response. This
        Reason String is a human readable string designed for diagnostics and is not intended to be parsed by
        the receiver.

        The sender uses this value to give additional information to the receiver. The sender MUST NOT send
        this property if it would increase the size of the PUBACK packet beyond the Maximum Packet Size
        specified by the receiver [MQTT-3.4.2-2]. It is a Protocol Error to include the Reason String more than
        once.
       */
      MqttMessageProperty.REASON_STRING,
      /*
        Followed by UTF-8 String Pair. This property can be used to provide additional diagnostic or other
        information. The sender MUST NOT send this property if it would increase the size of the PUBACK
        packet beyond the Maximum Packet Size specified by the receiver [MQTT-3.4.2-3]. The User Property is
        allowed to appear multiple times to represent multiple name, value pairs. The same name is allowed to
        appear more than once.
       */
      MqttMessageProperty.USER_PROPERTY);

  public PublishAckMqttInMessage(byte messageFlags) {
    super(messageFlags);
  }

  @Override
  public byte messageTypeId() {
    return (byte) MESSAGE_TYPE;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_ACK;
  }
  
  @Override
  protected PublishAckReasonCode defaultReasonCode() {
    return PublishAckReasonCode.SUCCESS;
  }

  @Override
  protected PublishAckReasonCode readReasonCode(int unsignedByte) {
    return PublishAckReasonCode.ofCode(unsignedByte);
  }

  @Override
  protected Set<MqttMessageProperty> availableProperties() {
    return AVAILABLE_PROPERTIES;
  }
  
  public static PublishAckMqttInMessage of(int messageId, PublishAckReasonCode reasonCode) {
    var message = new PublishAckMqttInMessage(MESSAGE_FLAGS);
    message.messageId = messageId;
    message.reasonCode = reasonCode;
    return message;
  }
}
