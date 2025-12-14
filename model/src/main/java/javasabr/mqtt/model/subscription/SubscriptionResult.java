package javasabr.mqtt.model.subscription;

import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.model.subscriber.SingleSubscriber;
import org.jspecify.annotations.Nullable;

public record SubscriptionResult(
    SubscribeAckReasonCode subscribeAckReasonCode,
    @Nullable SingleSubscriber subscriber,
    boolean isSubscriptionAlreadyExisted) {

  public SubscriptionResult(SingleSubscriber subscriber, boolean isSubscriptionAlreadyExisted) {
    this(subscriber.subscription().qos().subscribeAckReasonCode(), subscriber, isSubscriptionAlreadyExisted);
  }

  public SubscriptionResult(SubscribeAckReasonCode subscribeAckReasonCode) {
    this(subscribeAckReasonCode, null, false);
  }
}
