package javasabr.mqtt.service.session.impl;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.TrackableMessage;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.subscribtion.Subscription;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.MqttSession.UnsafeMqttSession;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.LockableArray;
import javasabr.rlib.collections.array.MutableArray;
import lombok.CustomLog;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@CustomLog
@ToString(of = "clientId")
@EqualsAndHashCode(of = "clientId")
@Accessors(fluent = true, chain = false)
public class InMemoryMqttSession implements UnsafeMqttSession {

  private static final Array<Subscription> EMPTY_SUBSCRIPTIONS = Array.empty(Subscription.class);

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
      MqttClient client,
      TrackableMessage response,
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

  private final String clientId;
  private final LockableArray<PendingPublish> pendingOutPublishes;
  private final LockableArray<PendingPublish> pendingInPublishes;
  private final AtomicInteger packetIdGenerator;
  private final LockableArray<Subscription> subscriptions;

  @Getter
  @Setter
  private volatile long expirationTime = -1;

  public InMemoryMqttSession(String clientId) {
    this.clientId = clientId;
    this.pendingOutPublishes = ArrayFactory.stampedLockBasedArray(PendingPublish.class);
    this.pendingInPublishes = ArrayFactory.stampedLockBasedArray(PendingPublish.class);
    this.packetIdGenerator = new AtomicInteger(0);
    this.subscriptions = ArrayFactory.stampedLockBasedArray(Subscription.class);
  }

  @Override
  public int nextMessageId() {

    var nextId = packetIdGenerator.incrementAndGet();

    if (nextId >= MqttProperties.MAXIMUM_PACKET_ID) {
      packetIdGenerator.compareAndSet(nextId, 0);
      return nextMessageId();
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
  public void registerInPublish(Publish publish, PendingMessageHandler handler) {
    registerPublish(publish, handler, pendingInPublishes);
  }

  @Override
  public boolean hasOutPending() {
    return !pendingOutPublishes.isEmpty();
  }

  @Override
  public boolean hasInPending() {
    return !pendingInPublishes.isEmpty();
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
  public boolean hasInPending(int messageId) {
    long stamp = pendingInPublishes.readLock();
    try {
      return pendingInPublishes
          .iterations()
          .findAny(messageId, (pending, targetId) -> pending.publish.messageId() == targetId) != null;
    } finally {
      pendingInPublishes.readUnlock(stamp);
    }
  }

  @Override
  public void resendPendingPackets(MqttClient client) {
    long stamp = pendingOutPublishes.readLock();
    try {
      for (PendingPublish pending : pendingOutPublishes) {
        PendingMessageHandler handler = pending.handler;
        Publish publish = pending.publish;
        handler.resend(client, publish);
      }
    } finally {
      pendingOutPublishes.readUnlock(stamp);
    }
  }

  @Override
  public void updateOutPendingPacket(MqttClient client, TrackableMessage response) {
    updatePendingPacket(client, response, pendingOutPublishes, clientId);
  }

  @Override
  public void updateInPendingPacket(MqttClient client, TrackableMessage response) {
    updatePendingPacket(client, response, pendingInPublishes, clientId);
  }

  @Override
  public void storeSubscription(Subscription subscription) {
    long stamp = subscriptions.writeLock();
    try {
      subscriptions.add(subscription);
    } finally {
      subscriptions.writeUnlock(stamp);
    }
  }

  @Override
  public void removeSubscription(TopicFilter topicFilter) {
    long stamp = subscriptions.writeLock();
    try {
      int index = subscriptions.indexOf(Subscription::topicFilter, topicFilter);
      if (index >= 0) {
        subscriptions.remove(index);
      }
    } finally {
      subscriptions.writeUnlock(stamp);
    }
  }

  @Override
  public Array<Subscription> storedSubscriptions() {
    if (subscriptions.isEmpty()) {
      return EMPTY_SUBSCRIPTIONS;
    }
    long stamp = subscriptions.readLock();
    try {
      return Array.copyOf(subscriptions);
    } finally {
      subscriptions.readUnlock(stamp);
    }
  }

  @Override
  public Array<Subscription> findStoredSubscriptionWithId(int subscriptionId) {
    if (subscriptions.isEmpty()) {
      return EMPTY_SUBSCRIPTIONS;
    }
    MutableArray<Subscription> result = ArrayFactory.mutableArray(Subscription.class);
    long stamp = subscriptions.readLock();
    try {
      for (Subscription subscription : subscriptions) {
        if (subscription.subscriptionId() == subscriptionId) {
          result.add(subscription);
        }
      }
    } finally {
      subscriptions.readUnlock(stamp);
    }
    return result;
  }

  @Override
  public void clear() {
    pendingInPublishes
        .operations()
        .inWriteLock(Collection::clear);
    pendingOutPublishes
        .operations()
        .inWriteLock(Collection::clear);
  }

  @Override
  public void onPersisted() {
    pendingInPublishes
        .operations()
        .inWriteLock(Collection::clear);
  }

  @Override
  public void onRestored() {
  }
}
