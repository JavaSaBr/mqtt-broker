package com.ss.mqtt.broker.test.model

import com.ss.mqtt.broker.model.topic.TopicFilter
import com.ss.mqtt.broker.model.topic.TopicName
import spock.lang.Specification
import spock.lang.Unroll

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
    def "should fail create topic name: #stringTopicName"(String stringTopicName, String errorMessage) {
        when:
            TopicName.from(stringTopicName)
        then:
            def ex = thrown IllegalArgumentException
            ex.message == errorMessage
        where:
            stringTopicName | errorMessage
            ""              | "Topic has zero length."
            "topic/+"       | "Single level wildcard is incorrectly used: topic/+"
            "topic/#"       | "Multi level wildcard is incorrectly used: topic/#"
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
    def "should fail create topic filter: #stringTopicFilter"(String stringTopicFilter, String errorMessage) {
        when:
            TopicFilter.from(stringTopicFilter)
        then:
            def ex = thrown IllegalArgumentException
            ex.message == errorMessage
        where:
            stringTopicFilter | errorMessage
            ""                | "Topic has zero length."
            "topic/in/"       | "Topic has zero length level: topic/in/"
            "/topic/in"       | "Topic has zero length level: /topic/in"
            "topic//in"       | "Topic has zero length level: topic//in"
            "topic/++/in"     | "Single level wildcard is incorrectly used: topic/++/in"
            "topic/#/in"      | "Multi level wildcard is incorrectly used: topic/#/in"
            "topic/##"        | "Multi level wildcard is incorrectly used: topic/##"
        
    }
    
}
