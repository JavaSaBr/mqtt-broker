package com.ss.mqtt.broker.test.model


import spock.lang.Specification
import spock.lang.Unroll

import static com.ss.mqtt.broker.util.TopicUtils.*

class TopicTest extends Specification {
    
    @Unroll
    def "should create topic name: #stringTopicName"(String stringTopicName, int levelsCount) {
        when:
            def topicName = newTopicName(stringTopicName)
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
            def topicName = newTopicName(stringTopicName)
        then:
            isInvalid(topicName)
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
            def topicFilter = newTopicFilter(stringTopicFilter)
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
            def topicFilter = newTopicFilter(stringTopicFilter)
        then:
            isInvalid(topicFilter)
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
