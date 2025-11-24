package javasabr.mqtt.network.message.in;

import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.TrackableMessage;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode;

/**
 * Publish received (QoS 2 delivery part 1).
 */
public class PublishReceivedMqttInMessage extends PublishControlMqttInMessage<PublishReceivedReasonCode>
    implements TrackableMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.PUBLISH_RECEIVED.ordinal();

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

  public  PublishReceivedMqttInMessage(byte messageFlags) {
    super(messageFlags);
  }

  @Override
  public byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  public String name() {
    return MqttMessageType.PUBLISH_RECEIVED.name();
  }

  @Override
  protected PublishReceivedReasonCode defaultReasonCode() {
    return PublishReceivedReasonCode.SUCCESS;
  }

  @Override
  protected PublishReceivedReasonCode readReasonCode(int unsignedByte) {
    return PublishReceivedReasonCode.ofCode(unsignedByte);
  }

  @Override
  protected Set<MqttMessageProperty> availableProperties() {
    return AVAILABLE_PROPERTIES;
  }
}
