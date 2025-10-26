package javasabr.mqtt.service.session.impl;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.subscriber.SubscribeTopicFilter;
import javasabr.mqtt.model.topic.TopicFilter;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.MqttSession.UnsafeMqttSession;
import javasabr.mqtt.network.message.HasMessageId;
import javasabr.mqtt.network.message.in.PublishMqttInMessage;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.LockableArray;
import javasabr.rlib.functions.TriConsumer;
import lombok.AllArgsConstructor;
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

  @Getter
  @AllArgsConstructor
  private static class PendingPublish {
    private final PublishMqttInMessage publish;
    private final PendingMessageHandler handler;
    private final int packetId;
  }

  private static void registerPublish(
      PublishMqttInMessage publish,
      PendingMessageHandler handler,
      int packetId,
      LockableArray<PendingPublish> pendingPublishes) {
    PendingPublish pendingPublish = new PendingPublish(publish, handler, packetId);
    pendingPublishes
        .operations()
        .inWriteLock(pendingPublish, Collection::add);
  }

  private static void updatePendingPacket(
      MqttClient client,
      HasMessageId response,
      LockableArray<PendingPublish> pendingPublishes,
      String clientId) {

    int packetId = response.messageId();
    PendingPublish pendingPublish;

    long stamp = pendingPublishes.readLock();
    try {
      pendingPublish = pendingPublishes
          .iterations()
          .findAny(packetId, (element, targetId) -> element.packetId == targetId);
    } finally {
      pendingPublishes.readUnlock(stamp);
    }

    if (pendingPublish == null) {
      log.warning(clientId , response, "Not found pending publish for client:[%s] by received packet:[%s]"::formatted);
      return;
    }

    var shouldBeRemoved = pendingPublish.handler.handleResponse(client, response);

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
  private final LockableArray<SubscribeTopicFilter> topicFilters;

  @Getter
  @Setter
  private volatile long expirationTime = -1;

  public InMemoryMqttSession(String clientId) {
    this.clientId = clientId;
    this.pendingOutPublishes = ArrayFactory.stampedLockBasedArray(PendingPublish.class);
    this.pendingInPublishes = ArrayFactory.stampedLockBasedArray(PendingPublish.class);
    this.packetIdGenerator = new AtomicInteger(0);
    this.topicFilters = ArrayFactory.stampedLockBasedArray(SubscribeTopicFilter.class);
  }

  @Override
  public int nextPacketId() {

    var nextId = packetIdGenerator.incrementAndGet();

    if (nextId >= MqttProperties.MAXIMUM_PACKET_ID) {
      packetIdGenerator.compareAndSet(nextId, 0);
      return nextPacketId();
    }

    return nextId;
  }

  @Override
  public String clientId() {
    return clientId;
  }

  @Override
  public void registerOutPublish(PublishMqttInMessage publish, PendingMessageHandler handler, int packetId) {
    registerPublish(publish, handler, packetId, pendingOutPublishes);
  }

  @Override
  public void registerInPublish(PublishMqttInMessage publish, PendingMessageHandler handler, int packetId) {
    registerPublish(publish, handler, packetId, pendingInPublishes);
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
  public boolean hasOutPending(int packetId) {
    long stamp = pendingOutPublishes.readLock();
    try {
      return pendingOutPublishes
          .iterations()
          .findAny(packetId, (element, targetId) -> element.packetId == targetId) != null;
    } finally {
      pendingOutPublishes.readUnlock(stamp);
    }
  }

  @Override
  public boolean hasInPending(int packetId) {
    long stamp = pendingInPublishes.readLock();
    try {
      return pendingInPublishes
          .iterations()
          .findAny(packetId, (element, targetId) -> element.packetId == targetId) != null;
    } finally {
      pendingInPublishes.readUnlock(stamp);
    }
  }

  @Override
  public void resendPendingPackets(MqttClient mqttClient) {
    long stamp = pendingOutPublishes.readLock();
    try {
      pendingOutPublishes
          .iterations()
          .forEach(
              mqttClient, (pendingPublish, client) -> {
                PendingMessageHandler handler = pendingPublish.handler;
                handler.resend(client, pendingPublish.publish, pendingPublish.packetId);
              });
    } finally {
      pendingOutPublishes.readUnlock(stamp);
    }
  }

  @Override
  public void updateOutPendingPacket(MqttClient client, HasMessageId response) {
    updatePendingPacket(client, response, pendingOutPublishes, clientId);
  }

  @Override
  public void updateInPendingPacket(MqttClient client, HasMessageId response) {
    updatePendingPacket(client, response, pendingInPublishes, clientId);
  }

  @Override
  public <A, B> void forEachTopicFilter(A arg1, B arg2, TriConsumer<A, B, SubscribeTopicFilter> consumer) {
    long stamp = topicFilters.readLock();
    try {
      for (SubscribeTopicFilter topicFilter : topicFilters) {
        consumer.accept(arg1, arg2, topicFilter);
      }
    } finally {
      topicFilters.readUnlock(stamp);
    }
  }

  @Override
  public void addSubscriber(SubscribeTopicFilter subscribe) {
    topicFilters
        .operations()
        .inWriteLock(subscribe, Collection::add);
  }

  @Override
  public void removeSubscriber(TopicFilter topicFilter) {
    long stamp = topicFilters.writeLock();
    try {
      int index = topicFilters.indexOf(SubscribeTopicFilter::getTopicFilter, topicFilter);
      if (index >= 0) {
        topicFilters.remove(index);
      }
    } finally {
      topicFilters.writeUnlock(stamp);
    }
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
