package com.ss.mqtt.broker.test.model

import com.ss.mqtt.broker.model.topic.TopicFilter
import com.ss.mqtt.broker.model.topic.TopicName
import spock.lang.Specification
import spock.lang.Unroll

import static com.ss.mqtt.broker.model.topic.TopicFilter.INVALID_TOPIC_FILTER
import static com.ss.mqtt.broker.model.topic.TopicName.INVALID_TOPIC_NAME

class TopicTest extends Specification {
    
    @Unroll
    def "should create topic name: #stringTopicName"(String stringTopicName, int levelsCount) {
        when:
            def topicName = TopicName.from(stringTopicName)
        then:
            topicName.segments.size() == levelsCount
            topicName.rawTopic == stringTopicName
            topicName.length == stringTopicName.length()
        where:
            stringTopicName   | levelsCount
            "topic/second/in" | 3
            "topic/second"    | 2
    }
    
    @Unroll
    def "should fail create topic name: #stringTopicName"(String stringTopicName) {
        when:
            def topicName = TopicName.from(stringTopicName)
        then:
            topicName == INVALID_TOPIC_NAME
        where:
            stringTopicName << [
                "",
                "topic/+",
                "topic/#"
            ]
    }
    
    @Unroll
    def "should create topic filter: #stringTopicFilter"(String stringTopicFilter, int levelsCount) {
        when:
            def topicFilter = TopicFilter.from(stringTopicFilter)
        then:
            topicFilter.segments.size() == levelsCount
            topicFilter.rawTopic == stringTopicFilter
            topicFilter.length == stringTopicFilter.length()
        where:
            stringTopicFilter | levelsCount
            "topic/in"        | 2
            "topic/+"         | 2
            "topic/#"         | 2
            "topic/+/in"      | 3
    }
    
    @Unroll
    def "should fail create topic filter: #stringTopicFilter"(String stringTopicFilter) {
        when:
            def topicFilter = TopicFilter.from(stringTopicFilter)
        then:
            topicFilter == INVALID_TOPIC_FILTER
        where:
            stringTopicFilter << [
                "",
                "topic/in/",
                "/topic/in",
                "topic//in",
                "topic/++/in",
                "topic/#/in",
                "topic/##"
            ]
    }
}
