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
}
