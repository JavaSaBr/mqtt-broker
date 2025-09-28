package javasabr.mqtt.application.util

import spock.lang.Specification
import spock.lang.Unroll

import static javasabr.mqtt.model.utils.TopicUtils.buildTopicFilter
import static javasabr.mqtt.model.utils.TopicUtils.buildTopicName
import static javasabr.mqtt.model.utils.TopicUtils.isInvalid

class TopicUtilsTest extends Specification {

  @Unroll
  def "should create valid topic name: [#topicName]"() {
    expect:
        !isInvalid(buildTopicName(topicName))
    where:
        topicName    | _
        "topic/Name" | _
        "topic"      | _
  }

  @Unroll
  def "should create valid topic filter: [#topicFilter]"() {
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
  def "should detect invalid topic name: [#topicName]"() {
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
  def "should detect invalid topic filter: [#topicFilter]"() {
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
