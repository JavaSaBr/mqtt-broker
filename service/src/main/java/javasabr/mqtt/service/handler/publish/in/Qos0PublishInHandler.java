package javasabr.mqtt.service.handler.publish.in;

import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.handler.publish.out.PublishOutHandler;

public class Qos0PublishInHandler extends AbstractPublishInHandler {

  public Qos0PublishInHandler(SubscriptionService subscriptionService, PublishOutHandler[] publishOutHandlers) {
    super(subscriptionService, publishOutHandlers);
  }
}
