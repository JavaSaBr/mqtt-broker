package com.ss.mqtt.broker.model.topic;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TopicFilter extends AbstractTopic {

  public TopicFilter(String topicFilter) {
    super(topicFilter);
  }
}

