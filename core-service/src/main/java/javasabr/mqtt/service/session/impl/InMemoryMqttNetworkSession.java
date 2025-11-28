package javasabr.mqtt.service.session.impl;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.message.TrackableMqttMessage;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.session.ActiveSubscriptions;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.ProcessingPublishes;
import javasabr.mqtt.model.session.TopicNameMapping;
import javasabr.mqtt.network.MqttNetworkSession.UnsafeMqttNetworkSession;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.LockableArray;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@CustomLog
@ToString(of = "clientId")
@EqualsAndHashCode(of = "clientId")
@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InMemoryMqttNetworkSession implements UnsafeMqttNetworkSession {

  private record PendingPublish(Publish publish, PendingMessageHandler handler) {}

  private static void registerPublish(
      Publish publish,
      PendingMessageHandler handler,
      LockableArray<PendingPublish> pendingPublishes) {
    PendingPublish pendingPublish = new PendingPublish(publish, handler);
    pendingPublishes
        .operations()
        .inWriteLock(pendingPublish, Collection::add);
  }

  private static void updatePendingPacket(
      NetworkMqttUser client,
      TrackableMqttMessage response,
      LockableArray<PendingPublish> pendingPublishes,
      String clientId) {

    int messageId = response.messageId();
    PendingPublish pendingPublish;

    long stamp = pendingPublishes.readLock();
    try {
      pendingPublish = pendingPublishes
          .iterations()
          .findAny(messageId, (pending, targetId) -> pending.publish.messageId() == targetId);
    } finally {
      pendingPublishes.readUnlock(stamp);
    }

    if (pendingPublish == null) {
      log.warning(clientId , response, "Not found pending publish for client:[%s] by received packet:[%s]"::formatted);
      return;
    }

    boolean shouldBeRemoved = pendingPublish.handler.handleResponse(client, response);
    if (shouldBeRemoved) {
      pendingPublishes
          .operations()
          .inWriteLock(pendingPublish, Collection::remove);
    }
  }

  final String clientId;
  final AtomicInteger messageIdGenerator;
  final LockableArray<PendingPublish> pendingOutPublishes;

  @Getter
  final MessageTacker inMessageTracker;
  @Getter
  final MessageTacker outMessageTracker;
  @Getter
  final ProcessingPublishes inProcessingPublishes;
  @Getter
  final ProcessingPublishes outProcessingPublishes;
  @Getter
  final ActiveSubscriptions activeSubscriptions;
  @Getter
  final TopicNameMapping topicNameMapping;

  @Getter
  @Setter
  volatile long expirationTime = -1;

  public InMemoryMqttNetworkSession(String clientId) {
    this.clientId = clientId;
    this.pendingOutPublishes = ArrayFactory.stampedLockBasedArray(PendingPublish.class);
    this.messageIdGenerator = new AtomicInteger(0);
    this.inMessageTracker = new InMemoryMessageTacker();
    this.outMessageTracker = new InMemoryMessageTacker();
    this.inProcessingPublishes = new InMemoryProcessingPublishes(this);
    this.outProcessingPublishes = new InMemoryProcessingPublishes(this);
    this.activeSubscriptions = new InMemoryActiveSubscriptions();
    this.topicNameMapping = new InMemoryTopicNameMapping();
  }

  @Override
  public int generateMessageId() {

    int nextId = messageIdGenerator.incrementAndGet();

    if (nextId >= MqttProperties.MAXIMUM_PACKET_ID) {
      messageIdGenerator.compareAndSet(nextId, 0);
      return generateMessageId();
    }

    return nextId;
  }

  @Override
  public String clientId() {
    return clientId;
  }

  @Override
  public void registerOutPublish(Publish publish, PendingMessageHandler handler) {
    registerPublish(publish, handler, pendingOutPublishes);
  }

  @Override
  public boolean hasOutPending() {
    return !pendingOutPublishes.isEmpty();
  }

  @Override
  public boolean hasOutPending(int messageId) {
    long stamp = pendingOutPublishes.readLock();
    try {
      return pendingOutPublishes
          .iterations()
          .findAny(messageId, (pending, targetId) -> pending.publish.messageId() == targetId) != null;
    } finally {
      pendingOutPublishes.readUnlock(stamp);
    }
  }

  @Override
  public void resendPendingPackets(NetworkMqttUser user) {
    long stamp = pendingOutPublishes.readLock();
    try {
      for (PendingPublish pending : pendingOutPublishes) {
        PendingMessageHandler handler = pending.handler;
        Publish publish = pending.publish;
        handler.resend(user, publish);
      }
    } finally {
      pendingOutPublishes.readUnlock(stamp);
    }
  }

  @Override
  public void updateOutPendingPacket(NetworkMqttUser user, TrackableMqttMessage response) {
    updatePendingPacket(user, response, pendingOutPublishes, clientId);
  }

  @Override
  public void clear() {
    pendingOutPublishes
        .operations()
        .inWriteLock(Collection::clear);
  }

  @Override
  public void onPersisted() {
  }

  @Override
  public void onRestored() {
  }
}
