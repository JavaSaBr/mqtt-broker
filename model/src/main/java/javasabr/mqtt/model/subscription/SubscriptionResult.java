package javasabr.mqtt.model.subscription;

import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import org.jspecify.annotations.Nullable;

public record SubscriptionResult(
    SubscribeAckReasonCode subscribeAckReasonCode,
    @Nullable Subscription newSubscription,
    @Nullable Subscription previousSubscription) {

  public SubscriptionResult(Subscription newSubscription, @Nullable Subscription previousSubscription) {
    this(newSubscription.qos().subscribeAckReasonCode(), newSubscription, previousSubscription);
  }

  public SubscriptionResult(SubscribeAckReasonCode subscribeAckReasonCode) {
    this(subscribeAckReasonCode, null, null);
  }

  public boolean isNotExistedPreviously(){
    return previousSubscription == null;
  }
}
