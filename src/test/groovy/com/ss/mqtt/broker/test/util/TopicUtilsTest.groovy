package com.ss.mqtt.broker.test.util


import spock.lang.Specification
import spock.lang.Unroll

import static com.ss.mqtt.broker.util.TopicUtils.*

class TopicUtilsTest extends Specification {
    
    @Unroll
    def "should create valid topic name: #topicName"(String topicName) {
        expect:
            !isInvalid(buildTopicName(topicName))
        where:
            topicName    | _
            "topic/Name" | _
            "topic"      | _
    }
    
    @Unroll
    def "should create valid topic filter: #topicFilter"(String topicFilter) {
        expect:
            !isInvalid(buildTopicFilter(topicFilter))
        where:
            topicFilter      | _
            "topic/Filter"   | _
            "topic/+"        | _
            "topic/+/Filter" | _
            "topic/#"        | _
    }
    
    @Unroll
    def "should detect invalid topic name: #topicName"(String topicName) {
        expect:
            isInvalid(buildTopicName(topicName))
        where:
            topicName     | _
            "topic/+"     | _
            "topic/"      | _
            "topic//Name" | _
            "topic/#"     | _
    }
    
    @Unroll
    def "should detect invalid topic filter: #topicFilter"(String topicFilter) {
        expect:
            isInvalid(buildTopicFilter(topicFilter))
        where:
            topicFilter   | _
            "topic/"      | _
            "/topic"      | _
            "topic//Name" | _
            "topic/##"    | _
            "#/Filter"    | _
    }
}
