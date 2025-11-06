package javasabr.mqtt.model.topic

import javasabr.mqtt.test.support.UnitSpecification
import spock.lang.Unroll

class TopicCreationTest extends UnitSpecification {

  @Unroll
  def "should create topic name:[#topicName] with levels [#levels] and segments #segments"() {
    given:
        def created = TopicName.valueOf(topicName)
    expect:
        created.levelsCount() == levels
        List.of(created.segments()) == segments
    where:
        topicName      | levels | segments
        "t"            | 1      | ["t"]
        "topic"        | 1      | ["topic"]
        "/"            | 2      | ["", ""]
        "/topic"       | 2      | ["", "topic"]
        "topic/"       | 2      | ["topic", ""]
        "/topic/name/" | 4      | ["", "topic", "name", ""]
        "topic/na  me" | 2      | ["topic", "na  me"]
  }

  @Unroll
  def "should create topic filter:[#topicFilter] with levels [#levels] and segments #segments"() {
    given:
        def created = TopicFilter.valueOf(topicFilter)
    expect:
        created.levelsCount() == levels
        List.of(created.segments()) == segments
    where:
        topicFilter             | levels | segments
        "t"                     | 1      | ["t"]
        "topic"                 | 1      | ["topic"]
        "/"                     | 2      | ["", ""]
        "/topic"                | 2      | ["", "topic"]
        "topic/"                | 2      | ["topic", ""]
        "/topic/name/"          | 4      | ["", "topic", "name", ""]
        "topic/na  me"          | 2      | ["topic", "na  me"]
        "#"                     | 1      | ["#"]
        "/#"                    | 2      | ["", "#"]
        "/topic/#"              | 3      | ["", "topic", "#"]
        "+"                     | 1      | ["+"]
        "/+"                    | 2      | ["", "+"]
        "+/+"                   | 2      | ["+", "+"]
        "/topic/+"              | 3      | ["", "topic", "+"]
        "/topic/+/filter"       | 4      | ["", "topic", "+", "filter"]
        "+/topic/+/filter"      | 4      | ["+", "topic", "+", "filter"]
        "topic/+/filter/+/test" | 5      | ["topic", "+", "filter", "+", "test"]
  }

  @Unroll
  def "should create shared topic filter:[#sharedTopicFilter] with levels [#levels], [#shareName] and segments #segments"() {
    given:
        def created = SharedTopicFilter.valueOf(sharedTopicFilter)
    expect:
        created.levelsCount() == levels
        created.shareName() == shareName
        List.of(created.segments()) == segments
    where:
        sharedTopicFilter                     | levels | shareName | segments
        '$share/name1/t'                      | 1      | "name1"   | ["t"]
        '$share/name2/topic'                  | 1      | "name2"   | ["topic"]
        '$share/name3/'                       | 1      | "name3"   | [""]
        '$share/name4/topic'                  | 1      | "name4"   | ["topic"]
        '$share/name5/topic/'                 | 2      | "name5"   | ["topic", ""]
        '$share/name6/topic/name/'            | 3      | "name6"   | ["topic", "name", ""]
        '$share/name7/topic/na  me'           | 2      | "name7"   | ["topic", "na  me"]
        '$share/name8/#'                      | 1      | "name8"   | ["#"]
        '$share/name9/#'                      | 1      | "name9"   | ["#"]
        '$share/name10/topic/#'               | 2      | "name10"  | ["topic", "#"]
        '$share/name11/+'                     | 1      | "name11"  | ["+"]
        '$share/name12/+'                     | 1      | "name12"  | ["+"]
        '$share/name13/+/+'                   | 2      | "name13"  | ["+", "+"]
        '$share/name14/topic/+'               | 2      | "name14"  | ["topic", "+"]
        '$share/name15/topic/+/filter'        | 3      | "name15"  | ["topic", "+", "filter"]
        '$share/name16/+/topic/+/filter'      | 4      | "name16"  | ["+", "topic", "+", "filter"]
        '$share/name17/topic/+/filter/+/test' | 5      | "name17"  | ["topic", "+", "filter", "+", "test"]
  }
}
