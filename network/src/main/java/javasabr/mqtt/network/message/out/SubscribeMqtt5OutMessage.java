package javasabr.mqtt.network.message.out;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.PacketProperty;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.SubscribeRetainHandling;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.subscriber.SubscribeTopicFilter;
import javasabr.mqtt.network.MqttConnection;
import javasabr.rlib.collections.array.Array;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Subscribe request.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubscribeMqtt5OutMessage extends SubscribeMqtt311OutMessage {

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

  // properties
  Array<StringPair> userProperties;
  int subscriptionId;

  public SubscribeMqtt5OutMessage(Array<SubscribeTopicFilter> topicFilters, int messageId) {
    this(topicFilters, messageId, Array.empty(StringPair.class), MqttProperties.SUBSCRIPTION_ID_UNDEFINED);
  }

  public SubscribeMqtt5OutMessage(
      Array<SubscribeTopicFilter> topicFilters,
      int messageId,
      Array<StringPair> userProperties,
      int subscriptionId) {
    super(topicFilters, messageId);
    this.userProperties = userProperties;
    this.subscriptionId = subscriptionId;
  }

  protected int buildSubscriptionOptions(SubscribeTopicFilter topicFilter) {

    SubscribeRetainHandling retainHandling = topicFilter.getRetainHandling();
    QoS qos = topicFilter.getQos();

    var subscriptionOptions = 0;
    subscriptionOptions |= retainHandling.ordinal() << 4;

    if (topicFilter.isRetainAsPublished()) {
      subscriptionOptions |= 0b0000_1000;
    }

    if (topicFilter.isNoLocal()) {
      subscriptionOptions |= 0b0000_0100;
    }

    subscriptionOptions |= qos.ordinal();

    return subscriptionOptions;
  }

  @Override
  protected boolean isPropertiesSupported(MqttConnection connection) {
    return true;
  }

  @Override
  protected void writeProperties(MqttConnection connection, ByteBuffer buffer) {
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901164
    writeStringPairProperties(buffer, PacketProperty.USER_PROPERTY, userProperties);
    writeProperty(
        buffer,
        PacketProperty.SUBSCRIPTION_IDENTIFIER,
        subscriptionId,
        MqttProperties.SUBSCRIPTION_ID_UNDEFINED);
  }
}
