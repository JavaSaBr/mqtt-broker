package javasabr.mqtt.network.message.in;

import static javasabr.mqtt.model.util.TopicUtils.buildTopicFilter;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.base.utils.DebugUtils;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.PacketProperty;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.SubscribeRetainHandling;
import javasabr.mqtt.model.subscriber.SubscribeTopicFilter;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.common.util.NumberUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Subscribe request.
 */
@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubscribeMqttInMessage extends MqttInMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.SUBSCRIBE.ordinal();

  static {
    DebugUtils.registerIncludedFields("messageId", "topicFilters");
  }

  private static final Set<PacketProperty> AVAILABLE_PROPERTIES = EnumSet.of(
      /*
        Followed by a Variable Byte Integer representing the identifier of the subscription. The Subscription
        Identifier can have the value of 1 to 268,435,455. It is a Protocol Error if the Subscription Identifier has a
        value of 0. It is a Protocol Error to include the Subscription Identifier more than once.

        The Subscription Identifier is associated with any subscription created or modified as the result of this
        SUBSCRIBE packet. If there is a Subscription Identifier, it is stored with the subscription. If this
        property is
        not specified, then the absence of a Subscription Identifier is stored with the subscription.
       */
      PacketProperty.SUBSCRIPTION_IDENTIFIER,
      /*
        The User Property is allowed to appear multiple times to represent multiple name, value pairs. The same
        name is allowed to appear more than once.
       */
      PacketProperty.USER_PROPERTY);

  MutableArray<SubscribeTopicFilter> topicFilters;
  int messageId;

  // properties
  int subscriptionId;

  public SubscribeMqttInMessage(byte info) {
    super(info);
    this.topicFilters = ArrayFactory.mutableArray(SubscribeTopicFilter.class);
    this.subscriptionId = MqttProperties.SUBSCRIPTION_ID_UNDEFINED;
  }

  @Override
  public byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  protected void readVariableHeader(MqttConnection connection, ByteBuffer buffer) {
    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718065
    messageId = readShortUnsigned(buffer);
  }

  @Override
  protected void readPayload(MqttConnection connection, ByteBuffer buffer) {

    if (buffer.remaining() < 1) {
      throw new IllegalStateException("No any topic filters.");
    }

    boolean isMqtt5 = connection.isSupported(MqttVersion.MQTT_5);

    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718066
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901168
    while (buffer.hasRemaining()) {

      String topicFilter = readString(buffer, Integer.MAX_VALUE);
      int options = readByteUnsigned(buffer);

      QoS qos = QoS.of(options & 0x03);
      SubscribeRetainHandling retainHandling = isMqtt5 ? SubscribeRetainHandling.of((options >> 4) & 0x03) : SubscribeRetainHandling.SEND;

      if (qos == QoS.INVALID || retainHandling == SubscribeRetainHandling.INVALID) {
        throw new IllegalStateException("Unsupported qos or retain handling");
      }

      boolean noLocal = !isMqtt5 || NumberUtils.isSetBit(options, 2);
      boolean rap = !isMqtt5 || NumberUtils.isSetBit(options, 3);

      topicFilters.add(new SubscribeTopicFilter(buildTopicFilter(topicFilter), qos, retainHandling, noLocal, rap));
    }
  }

  @Override
  protected Set<PacketProperty> availableProperties() {
    return AVAILABLE_PROPERTIES;
  }

  @Override
  protected void applyProperty(PacketProperty property, long value) {
    switch (property) {
      case SUBSCRIPTION_IDENTIFIER -> subscriptionId = (int) value;
      default -> unexpectedProperty(property);
    }
  }
}
