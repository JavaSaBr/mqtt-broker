package javasabr.mqtt.service.session.impl;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.TrackableMessage;
import javasabr.mqtt.model.publishing.Publish;
import javasabr.mqtt.model.session.ActiveSubscriptions;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.PendingPublishers;
import javasabr.mqtt.model.session.TopicNameMapping;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.session.MqttSession.UnsafeMqttSession;
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
public class InMemoryMqttSession implements UnsafeMqttSession {

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

  final String clientId;
  final AtomicInteger messageIdGenerator;
  final LockableArray<PendingPublish> pendingOutPublishes;
  final LockableArray<PendingPublish> pendingInPublishes;

  @Getter
  final MessageTacker inMessageTracker;
  @Getter
  final MessageTacker outMessageTracker;
  @Getter
  final PendingPublishers inPendingPublishers;
  @Getter
  final PendingPublishers outPendingPublishers;
  @Getter
  final ActiveSubscriptions activeSubscriptions;
  @Getter
  final TopicNameMapping topicNameMapping;

  @Getter
  @Setter
  volatile long expirationTime = -1;

  public InMemoryMqttSession(String clientId) {
    this.clientId = clientId;
    this.pendingOutPublishes = ArrayFactory.stampedLockBasedArray(PendingPublish.class);
    this.pendingInPublishes = ArrayFactory.stampedLockBasedArray(PendingPublish.class);
    this.messageIdGenerator = new AtomicInteger(0);
    this.inMessageTracker = new InMemoryMessageTacker();
    this.outMessageTracker = new InMemoryMessageTacker();
    this.inPendingPublishers = new InMemoryPendingPublishers(this);
    this.outPendingPublishers = new InMemoryPendingPublishers(this);
    this.activeSubscriptions = new InMemoryActiveSubscriptions();
    this.topicNameMapping = new InMemoryTopicNameMapping();
  }

  @Override
  public int nextMessageId() {

    int nextId = messageIdGenerator.incrementAndGet();

    if (nextId >= MqttProperties.MAXIMUM_PACKET_ID) {
      messageIdGenerator.compareAndSet(nextId, 0);
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
