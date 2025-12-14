package javasabr.mqtt.service.session.impl;

import java.util.concurrent.atomic.AtomicInteger;
import javasabr.mqtt.model.MqttProperties;
import javasabr.mqtt.network.session.ConfigurableNetworkMqttSession;
import javasabr.mqtt.network.user.NetworkMqttUser;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@CustomLog
@Accessors
@ToString(of = "clientId")
@EqualsAndHashCode(of = "clientId")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InMemoryNetworkMqttSession implements ConfigurableNetworkMqttSession {
  
  final String clientId;
  final AtomicInteger messageIdGenerator;

  @Getter
  final InMemoryMessageTacker inMessageTracker;
  @Getter
  final InMemoryMessageTacker outMessageTracker;
  @Getter
  final InMemoryProcessingPublishes inProcessingPublishes;
  @Getter
  final InMemoryProcessingPublishes outProcessingPublishes;
  @Getter
  final InMemoryActiveSubscriptions activeSubscriptions;
  @Getter
  final InMemoryTopicNameMapping topicNameMapping;

  @Getter
  @Setter
  volatile long expirationTime = -1;

  public InMemoryNetworkMqttSession(String clientId) {
    this.clientId = clientId;
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
  public String clientId() {
    return clientId;
  }

  @Override
  public int resendNotConfirmedPublishesTo(NetworkMqttUser user) {
    return outProcessingPublishes.resendTo(user);
  }
  
  public void clear() {
    inMessageTracker.clear();
    outMessageTracker.clear();
    inProcessingPublishes.clear();
    outProcessingPublishes.clear();
    activeSubscriptions.clear();
    topicNameMapping.clear();
  }
}
