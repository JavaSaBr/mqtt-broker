package com.ss.mqtt.broker.model.topic;

import com.ss.mqtt.broker.util.TopicUtils;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class TopicFilter extends AbstractTopic {

    public static final TopicFilter INVALID_TOPIC_FILTER = new TopicFilter();

    public static @NotNull TopicFilter from(@NotNull String topicFilter) {
        if (!TopicUtils.check(topicFilter) || topicFilter.contains("++")) {
            return INVALID_TOPIC_FILTER;
        }
        int multiPos = topicFilter.indexOf(MULTI_LEVEL_WILDCARD);
        if (multiPos != -1 && multiPos != topicFilter.length() - 1) {
            return INVALID_TOPIC_FILTER;
        } else if(topicFilter.startsWith("$shared")) {
            int firstSlash = topicFilter.indexOf(DELIMITER) + 1;
            int secondSlash = topicFilter.indexOf(DELIMITER, firstSlash);
            var group = topicFilter.substring(firstSlash, secondSlash);
            var realTopicFilter = topicFilter.substring(secondSlash + 1);
            return new SharedTopicFilter(realTopicFilter, group);
        } else {
            return new TopicFilter(topicFilter);
        }
    }

    TopicFilter(@NotNull String topicFilter) {
        super(topicFilter);
    }
}

