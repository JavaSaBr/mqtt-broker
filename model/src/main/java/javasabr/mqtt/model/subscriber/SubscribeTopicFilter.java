package javasabr.mqtt.model.subscriber;

import static javasabr.mqtt.model.utils.TopicUtils.buildTopicFilter;

import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.SubscribeRetainHandling;
import javasabr.mqtt.model.topic.TopicFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class SubscribeTopicFilter {

  /**
   * The subscriber's topic filter.
   */
  private final TopicFilter topicFilter;

  /**
   * Maximum QoS field. This gives the maximum QoS level at which the Server can send Application Messages to the
   * Client.
   */
  private final QoS qos;

  /**
   * This option specifies whether retained messages are sent when the subscription is established. This does not affect
   * the sending of retained messages at any point after the subscribe. If there are no retained messages matching the
   * Topic Filter, all of these values act the same.
   */
  private final SubscribeRetainHandling retainHandling;

  /**
   * If the value is true, Application Messages MUST NOT be forwarded to a connection with a ClientID equal to the
   * ClientID of the publishing connection.
   */
  private final boolean noLocal;

  /**
   * If true, Application Messages forwarded using this subscription keep the RETAIN flag they were published with. If
   * false, Application Messages forwarded using this subscription have the RETAIN flag set to 0. Retained messages sent
   * when the subscription is established have the RETAIN flag set to 1.
   */
  private final boolean retainAsPublished;

  public SubscribeTopicFilter(String topicFilter, QoS qos) {
    this(buildTopicFilter(topicFilter), qos, SubscribeRetainHandling.SEND, true, true);
  }

  public SubscribeTopicFilter(TopicFilter topicFilter, QoS qos) {
    this(topicFilter, qos, SubscribeRetainHandling.SEND, true, true);
  }

  @Override
  public String toString() {
    return "SubscribeTopicFilter(" + "topicFilter=" + topicFilter.getRawTopic() + ", qos=" + qos + ", retainHandling="
        + retainHandling + ", noLocal=" + noLocal + ", retainAsPublished=" + retainAsPublished + ')';
  }
}
