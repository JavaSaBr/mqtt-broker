package javasabr.mqtt.model.subscription;

import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import org.jspecify.annotations.Nullable;

public record SubscriptionResult(
    SubscribeAckReasonCode subscribeAckReasonCode,
    @Nullable Subscription subscription,
    boolean isSubscriptionAlreadyExisted) {

  public SubscriptionResult(Subscription subscription, boolean isSubscriptionAlreadyExisted) {
    this(subscription.qos().subscribeAckReasonCode(), subscription, isSubscriptionAlreadyExisted);
  }

  public SubscriptionResult(SubscribeAckReasonCode subscribeAckReasonCode) {
    this(subscribeAckReasonCode, null, false);
  }

  public boolean isNotExistedPreviously(){
    return !isSubscriptionAlreadyExisted;
  }
}
