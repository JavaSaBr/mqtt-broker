package javasabr.mqtt.service;

import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.subscription.Subscription;
import javasabr.rlib.collections.array.Array;

public interface RetainMessageService {

  void retainMessage(Publish publish);

  Array<Publish> getRetainedMessages(Subscription subscription);
}
