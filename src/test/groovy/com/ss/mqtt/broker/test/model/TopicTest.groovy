package com.ss.mqtt.broker.test.model


import spock.lang.Specification
import spock.lang.Unroll

import static com.ss.mqtt.broker.util.TopicUtils.*

class TopicTest extends Specification {
    
    @Unroll
    def "should create topic name: [#stringTopicName]"() {
        when:
            def topicName = buildTopicName(stringTopicName)
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
    def "should fail create topic name: [#stringTopicName]"() {
        expect:
            isInvalid(buildTopicName(stringTopicName))
        where:
            stringTopicName << [
                "",
                "topic/+",
                "topic/#"
            ]
    }
    
    @Unroll
    def "should create topic filter: [#stringTopicFilter]"() {
        when:
            def topicFilter = buildTopicFilter(stringTopicFilter)
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
    def "should fail create topic filter: [#stringTopicFilter]"() {
        expect:
            isInvalid(buildTopicFilter(stringTopicFilter))
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
    
    @Unroll
    def "should match topic filter: [#topicFilter] with topic name: [#topicName]"() {
        expect:
            buildTopicName(topicName).match(buildTopicFilter(topicFilter))
        where:
            topicFilter  | topicName
            "topic/in"   | "topic/in"
            "topic/+"    | "topic/in"
            "topic/#"    | "topic/in"
            "topic/+/in" | "topic/m/in"
    }
    
    @Unroll
    def "should not match topic filter: [#topicFilter] with topic name: [#topicName]"() {
        expect:
            !buildTopicName(topicName).match(buildTopicFilter(topicFilter))
        where:
            topicFilter  | topicName
            "topic/in"   | "topic/m/in"
            "topic/in"   | "topic/in/m"
            "topic/+"    | "topic/m/in"
            "topic/+"    | "topic/in/m"
            "topic/#"    | "topic"
            "topic/+/in" | "topic/m/n"
            "topic/+/in" | "topic/in"
    }
}
