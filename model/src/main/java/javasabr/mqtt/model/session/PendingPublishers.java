package javasabr.mqtt.model.session;

import javasabr.mqtt.model.publishing.Publish;

public interface PendingPublishers {

  void register(Publish publish, TrackableMessageCallback callback, PublishRetryer retryer);
}
