package javasabr.mqtt.model.subscription;

import com.fasterxml.jackson.annotation.JsonValue;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.SubscribeRetainHandling;
import javasabr.mqtt.model.topic.TopicFilter;

public record Subscription(
    /*
      The subscriber's topic filter.
     */
    TopicFilter topicFilter,
    /*
     * The associated ID for the subscription
     */
    int subscriptionId,
    /*
      Maximum QoS field. This gives the maximum QoS level at which the Server can send Application Messages to the
      Client.
     */
    QoS qos,
    /*
      This option specifies whether retained messages are sent when the subscription is established. This does not affect
      the sending of retained messages at any point after the subscribe. If there are no retained messages matching the
      Topic Filter, all of these values act the same.
     */
    SubscribeRetainHandling retainHandling,
    /*
      If the value is true, Application Messages MUST NOT be forwarded to a connection with a ClientID equal to the
      ClientID of the publishing connection.
     */
    boolean noLocal,
    /*
      If true, Application Messages forwarded using this subscription keep the RETAIN flag they were published with. If
      false, Application Messages forwarded using this subscription have the RETAIN flag set to 0.

      Bit 3 of the Subscription Options represents the Retain As Published option.
      If 1, Application Messages forwarded using this subscription keep the RETAIN flag they were published with.
      If 0, Application Messages forwarded using this subscription have the RETAIN flag set to 0.
      Retained messages sent when the subscription is established have the RETAIN flag set to 1.
     */
    boolean retainAsPublished) {

  public static Subscription minimal(TopicFilter topicFilter, QoS qos) {
    return new Subscription(
        topicFilter,
        MqttProperties.SUBSCRIPTION_ID_IS_NOT_SET,
        qos,
        SubscribeRetainHandling.SEND,
        true,
        true);
  }

  @JsonValue
  @Override
  public String toString() {
    return "[" + topicFilter.rawTopic() + "|" + qos.level() + "|" + retainHandling + "|" + noLocal + "|" + retainAsPublished + "]";
  }
}
