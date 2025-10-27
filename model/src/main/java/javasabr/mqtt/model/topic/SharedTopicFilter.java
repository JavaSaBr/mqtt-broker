package javasabr.mqtt.model.topic;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SharedTopicFilter extends TopicFilter {

  String group;

  public SharedTopicFilter(String topicFilter, String group) {
    super(topicFilter);
    this.group = group;
  }
}

