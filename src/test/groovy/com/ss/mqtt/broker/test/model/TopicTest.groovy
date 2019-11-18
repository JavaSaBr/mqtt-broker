package com.ss.mqtt.broker.test.model

import com.ss.mqtt.broker.model.TopicFilter
import com.ss.mqtt.broker.model.TopicName
import spock.lang.Specification
import spock.lang.Unroll

class TopicTest extends Specification {
    
    @Unroll
    def "should not match #stringTopicFilter to #stringTopicName"(String stringTopicFilter, String stringTopicName) {
        given:
            def topicFilter = new TopicFilter(stringTopicFilter)
            def topicName = new TopicName(stringTopicName)
        when:
            def result = topicFilter.matches(topicName)
        then:
            !result
        where:
            stringTopicFilter | stringTopicName
            "topic/second/in" | "topic/first/in"
            "topic/second"    | "topic/first/in"
    }
    
    @Unroll
    def "should match #stringTopicFilter to #stringTopicName"(String stringTopicFilter, String stringTopicName) {
        given:
            def topicFilter = new TopicFilter(stringTopicFilter)
            def topicName = new TopicName(stringTopicName)
        when:
            def result = topicFilter.matches(topicName)
        then:
            result
        where:
            stringTopicFilter | stringTopicName
            "topic/in"        | "topic/in"
            "topic/+"         | "topic/in"
            "topic/#"         | "topic/first/in"
            "topic/+/in"      | "topic/first/in"
    }
    
    @Unroll
    def "should fail create topic filter: #stringTopicFilter"(String stringTopicFilter, String errorMessage) {
        when:
            new TopicFilter(stringTopicFilter)
        then:
            def ex = thrown IllegalArgumentException
            ex.message == errorMessage
        where:
            stringTopicFilter | errorMessage
            "topic/in/"       | "Topic name has zero length level: topic/in/"
            "/topic/in"       | "Topic name has zero length level: /topic/in"
            "topic//in"       | "Topic name has zero length level: topic//in"
            "topic/++/in"     | "Single level wildcard is incorrectly used: topic/++/in"
            "topic/#/in"      | "Multi level wildcard is incorrectly used: topic/#/in"
            "topic/##"        | "Multi level wildcard is incorrectly used: topic/##"
        
    }
    
}
