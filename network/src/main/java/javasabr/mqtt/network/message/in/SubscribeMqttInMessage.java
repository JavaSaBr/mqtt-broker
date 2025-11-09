package javasabr.mqtt.network.message.in;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.mqtt.model.MqttMessageProperty;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.SubscribeRetainHandling;
import javasabr.mqtt.model.exception.MalformedProtocolMqttException;
import javasabr.mqtt.model.subscribtion.RequestedSubscription;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Subscribe request.
 */
@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PROTECTED)
public class SubscribeMqttInMessage extends TrackableMqttInMessage {

  private static final byte MESSAGE_TYPE = (byte) MqttMessageType.SUBSCRIBE.ordinal();

  static {
    DebugUtils.registerIncludedFields("subscriptions");
  }

  private static final Set<MqttMessageProperty> AVAILABLE_PROPERTIES = EnumSet.of(
      /*
        Followed by a Variable Byte Integer representing the identifier of the subscription. The Subscription
        Identifier can have the value of 1 to 268,435,455. It is a Protocol Error if the Subscription Identifier has a
        value of 0. It is a Protocol Error to include the Subscription Identifier more than once.

        The Subscription Identifier is associated with any subscription created or modified as the result of this
        SUBSCRIBE packet. If there is a Subscription Identifier, it is stored with the subscription. If this
        property is not specified, then the absence of a Subscription Identifier is stored with the subscription.
       */
      MqttMessageProperty.SUBSCRIPTION_IDENTIFIER,
      /*
        The User Property is allowed to appear multiple times to represent multiple name, value pairs. The same
        name is allowed to appear more than once.
       */
      MqttMessageProperty.USER_PROPERTY);

  final MutableArray<RequestedSubscription> subscriptions;

  // properties
  int subscriptionId;

  public SubscribeMqttInMessage(byte info) {
    super(info);
    this.subscriptions = ArrayFactory.mutableArray(RequestedSubscription.class);
    this.subscriptionId = MqttProperties.SUBSCRIPTION_ID_UNDEFINED;
  }

  @Override
  public byte messageType() {
    return MESSAGE_TYPE;
  }

  @Override
  protected void readPayload(MqttConnection connection, ByteBuffer buffer) {
    if (buffer.remaining() < 1) {
      throw new MalformedProtocolMqttException("No any topic filters");
    }

    MqttServerConnectionConfig severConnConfig = connection.serverConnectionConfig();
    boolean isMqtt5 = connection.isSupported(MqttVersion.MQTT_5);

    // http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718066
    // https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901168
    while (buffer.hasRemaining()) {
      String topicFilter = readString(buffer, severConnConfig.maxStringLength());

      int options = readByteUnsigned(buffer);
      int qosLevel = options & 0b0000_0011;
      boolean noLocal = true;
      boolean retainAsPublished = true;
      SubscribeRetainHandling retainHandling = SubscribeRetainHandling.SEND;

      if (isMqtt5) {
        noLocal = (options & 0b0000_0100) != 0;
        retainAsPublished = (options & 0b0000_1000) != 0;
        int retainLevel = (options & 0b0011_0000) >> 4;
        retainHandling =  SubscribeRetainHandling.of(retainLevel);
      } else {
        validateMqtt311Options(options);
      }

      QoS qos = QoS.ofCode(qosLevel);
      if (qos == QoS.INVALID || retainHandling == SubscribeRetainHandling.INVALID) {
        throw new MalformedProtocolMqttException("Unsupported qos or retain handling");
      }

      subscriptions.add(new RequestedSubscription(
          topicFilter,
          qos,
          retainHandling,
          noLocal,
          retainAsPublished));
    }
  }

  @Override
  protected Set<MqttMessageProperty> availableProperties() {
    return AVAILABLE_PROPERTIES;
  }

  @Override
  protected void applyProperty(MqttMessageProperty property, long value) {
    switch (property) {
      case SUBSCRIPTION_IDENTIFIER -> subscriptionId = (int) value;
      default -> unexpectedProperty(property);
    }
  }

  public Array<RequestedSubscription> subscriptions() {
    return subscriptions;
  }

  public int subscriptionsCount() {
    return subscriptions.size();
  }

  private static void validateMqtt311Options(int options) {
    // for MQTT 3.1.1 these bits must be zero
    if ((options & 0b0000_0100) != 0) {
      throw new MalformedProtocolMqttException("No local option is not available on this protocol level");
    } else if ((options & 0b0000_1000) != 0) {
      throw new MalformedProtocolMqttException("Retain as published option is not available on this protocol level");
    } else if (((options & 0b0011_0000) >> 4) != 0) {
      throw new MalformedProtocolMqttException("Retain level option is not available on this protocol level");
    }
  }
}
