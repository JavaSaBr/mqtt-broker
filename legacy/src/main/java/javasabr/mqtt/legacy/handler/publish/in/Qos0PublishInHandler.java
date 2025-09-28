package javasabr.mqtt.legacy.handler.publish.in;

import javasabr.mqtt.legacy.handler.publish.out.PublishOutHandler;
import javasabr.mqtt.legacy.service.SubscriptionService;

public class Qos0PublishInHandler extends AbstractPublishInHandler {

  public Qos0PublishInHandler(SubscriptionService subscriptionService, PublishOutHandler[] publishOutHandlers) {
    super(subscriptionService, publishOutHandlers);
  }
}
