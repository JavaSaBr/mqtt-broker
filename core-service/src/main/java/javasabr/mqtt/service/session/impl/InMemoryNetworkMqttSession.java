package javasabr.mqtt.service.session.impl;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.publish.OutgoingPublish;
import javasabr.mqtt.network.session.ConfigurableNetworkMqttSession;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.rlib.collections.array.MutableIntArray;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@CustomLog
@Accessors
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InMemoryNetworkMqttSession implements ConfigurableNetworkMqttSession {
  
  @EqualsAndHashCode.Include
  final String clientId;
  final long internalId;
  final AtomicInteger messageIdGenerator;
  final AtomicLong dataIdGenerator;
  final AtomicLong publishIdGenerator;

  @Getter
  final InMemoryMessageTacker inMessageTracker;
  @Getter
  final InMemoryMessageTacker outMessageTracker;
  @Getter
  final InMemoryProcessingPublishes<IncomingPublish> incomingProcessingPublishes;
  @Getter
  final InMemoryProcessingPublishes<OutgoingPublish> outgoingProcessingPublishes;
  @Getter
  final InMemoryActiveSubscriptions activeSubscriptions;
  @Getter
  final InMemoryTopicNameMapping topicNameMapping;

  @Getter
  @Setter
  volatile Duration expiryInterval;

  public InMemoryNetworkMqttSession(String clientId, long internalId) {
    this.clientId = clientId;
    this.internalId = internalId;
    this.messageIdGenerator = new AtomicInteger(0);
    this.dataIdGenerator = new AtomicLong(0);
    this.publishIdGenerator = new AtomicLong(0);
    this.inMessageTracker = new InMemoryMessageTacker();
    this.outMessageTracker = new InMemoryMessageTacker();
    this.incomingProcessingPublishes = new InMemoryProcessingPublishes<>(this);
    this.outgoingProcessingPublishes = new InMemoryProcessingPublishes<>(this);
    this.activeSubscriptions = new InMemoryActiveSubscriptions();
    this.topicNameMapping = new InMemoryTopicNameMapping();
    this.expiryInterval = MqttProperties.SESSION_EXPIRY_DURATION_DISABLED;
  }

  @Override
  public int generateMessageId() {
    int nextId;
    do {
      nextId = messageIdGenerator.incrementAndGet();
      if (nextId >= MqttProperties.MAXIMUM_PACKET_ID) {
        messageIdGenerator.compareAndSet(nextId, 0);
      }
    } while (nextId >= MqttProperties.MAXIMUM_PACKET_ID);
    return nextId;
  }

  @Override
  public UUID generateDataId() {
    return new UUID(internalId, dataIdGenerator.incrementAndGet());
  }

  @Override
  public UUID generatePublishId() {
    return new UUID(internalId, publishIdGenerator.incrementAndGet());
  }

  @Override
  public String clientId() {
    return clientId;
  }

  @Override
  public int resendNotConfirmedPublishesTo(NetworkMqttUser user) {
    return outgoingProcessingPublishes.resendTo(user);
  }
  
  public void clear() {
    inMessageTracker.clear();
    outMessageTracker.clear();
    incomingProcessingPublishes.clear();
    outgoingProcessingPublishes.clear();
    activeSubscriptions.clear();
    topicNameMapping.clear();
  }
 
  public void update(long currentTimeInMs, MutableIntArray calculation) {
    inMessageTracker.cleanupExpired(currentTimeInMs, calculation);
    outMessageTracker.cleanupExpired(currentTimeInMs, calculation);
  }
}
