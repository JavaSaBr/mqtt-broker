package com.ss.mqtt.broker.model.topic;

import lombok.Getter;

public class SharedTopicFilter extends TopicFilter {

  @Getter
  private final String group;

  public SharedTopicFilter(String topicFilter, String group) {
    super(topicFilter);
    this.group = group;
  }
}

