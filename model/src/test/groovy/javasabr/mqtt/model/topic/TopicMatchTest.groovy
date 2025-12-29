package javasabr.mqtt.model.topic

import javasabr.mqtt.test.support.UnitSpecification
import spock.lang.Unroll

class TopicMatchTest extends UnitSpecification {

  @Unroll
  def "should match:[#topicFilter] to [#topicName] with [#expectedResult]"() {
    given:
        def createdTopicFilter = TopicFilter.valueOf(topicFilter)
        def createdTopicName = TopicName.valueOf(topicName)
    expect:
        createdTopicFilter.isMatched(createdTopicName) == expectedResult
    where:
        topicFilter                | topicName            | expectedResult
        'topic/name/segment'       | 'topic/name/segment' | true
        'topic/name/+'             | 'topic/name/segment' | true
        'topic/+/+'                | 'topic/name/segment' | true
        'topic/+/segment'          | 'topic/name/segment' | true
        '+/+/segment'              | 'topic/name/segment' | true
        '+/+/+'                    | 'topic/name/segment' | true
        '+/name/segment'           | 'topic/name/segment' | true
        'topic/name/#'             | 'topic/name/segment' | true
        'topic/#'                  | 'topic/name/segment' | true
        '#'                        | 'topic/name/segment' | true
        'topic/+'                  | 'topic/name/segment' | false
        '+'                        | 'topic/name/segment' | false
        'topic/name/segment/value' | 'topic/name/segment' | false
        'topic/name2/segment'      | 'topic/name/segment' | false
        'topic/name/segment2'      | 'topic/name/segment' | false
        'topic2/name/segment'      | 'topic/name/segment' | false
        '+/name2/segment'          | 'topic/name/segment' | false
  }

  @Unroll
  def "should match:[#topicName] to [#topicFilter] with [#expectedResult]"() {
    given:
        def createdTopicName = TopicName.valueOf(topicName)
        def createdTopicFilter = TopicFilter.valueOf(topicFilter)
    expect:
        createdTopicName.isMatched(createdTopicFilter) == expectedResult
    where:
        topicName            | topicFilter          | expectedResult
        'topic/name/segment' | 'topic/name/segment' | true
        'topic/name'         | 'topic/name'         | true
        'topic/'             | 'topic/'             | true
        'topic'              | 'topic'              | true

        'topic/name/segment' | 'topic/name/+'       | false
        'topic/name/segment' | 'topic/+/+'          | false
        'topic/name/segment' | 'topic/+/segment'    | false
        'topic/name/segment' | '+/+/+'              | false
        'topic/name/segment' | '+/name/segment'     | false
        'topic/name/segment' | 'topic/#'            | false
        'topic/name/segment' | '#'                  | false
        'topic/name/segment' | 'topic/name'         | false
        'topic/name'         | 'topic/name/segment' | false
        'topic/name/segment' | 'topic/'             | false
        'topic/n'            | 'topic/name/segment' | false
  }
}
