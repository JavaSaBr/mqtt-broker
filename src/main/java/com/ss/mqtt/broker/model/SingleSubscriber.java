package com.ss.mqtt.broker.model;

import com.ss.mqtt.broker.network.client.MqttClient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode(of = "mqttClient")
@RequiredArgsConstructor
public final class SingleSubscriber implements Subscriber {

  @Getter
  private final MqttClient mqttClient;
  private final SubscribeTopicFilter subscribe;

  public QoS getQos() {
    return subscribe.getQos();
  }
}
