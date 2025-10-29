package javasabr.mqtt.model.topic

import javasabr.mqtt.test.support.UnitSpecification
import spock.lang.Unroll

class TopicValidatorTest extends UnitSpecification {

  private static final String NULL_CHAR = "\u0000"

  @Unroll
  def "should validate topic name:[#topicName]->[#valid]"() {
    expect:
        TopicValidator.validateTopicName(topicName) == valid
    where:
        topicName           | valid
        "t"                 | true
        "/"                 | true
        "topic"             | true
        "topic/name"        | true
        "topic/name/"       | true
        "/topic/name/"      | true
        "/to  pic/nam e/"   | true
        "/topic"            | true
        ""                  | false
        "//"                | false
        "+"                 | false
        "+/+"               | false
        "#"                 | false
        "/to$NULL_CHAR/ne/" | false
  }

  @Unroll
  def 'should validate topic filter:[#topicFilter]->[#valid]'() {
    expect:
        TopicValidator.validateTopicFilter(topicFilter) == valid
    where:
        topicFilter          | valid
        "t"                  | true
        "/"                  | true
        "topic"              | true
        "topic/filter"       | true
        "topic/filter/"      | true
        "/topic/filter/"     | true
        "/topic"             | true
        "+"                  | true
        "+/+"                | true
        "/+/"                | true
        "+/filter"           | true
        "+/filter/+"         | true
        "+/ fil  ter/+"      | true
        "+/filter/+/segment" | true
        "#"                  | true
        "+/filter/#"         | true
        "/topic/#"           | true
        ""                   | false
        "//"                 | false
        "/to$NULL_CHAR/ne/"  | false
        "#/filter"           | false
        "++"                 | false
        "##"                 | false
        "topic/filter+"      | false
        "+/+topic/filter"    | false
        "+/#/filter"         | false
        "#/filter"           | false
  }

  @Unroll
  def 'should validate shared topic filter:[#sharedTopicFilter]->[#valid]'() {
    expect:
        TopicValidator.validateSharedTopicFilter(sharedTopicFilter) == valid
    where:
        sharedTopicFilter                  | valid
        '$share/group1/t'                  | true
        '$share/group1/topic'              | true
        '$share/group1/topic/filter'       | true
        '$share/group1/topic/filter/'      | true
        '$share/group1/topic/filter/'      | true
        '$share/group1/topic'              | true
        '$share/group1/+'                  | true
        '$share/group1/+/+'                | true
        '$share/group1/+/'                 | true
        '$share/group1/+/filter'           | true
        '$share/group1/+/filter/+'         | true
        '$share/group1/+/fi  lt er/+'      | true
        '$share/group1/+/filter/+/segment' | true
        '$share/group1/#'                  | true
        '$share/group1/+/filter/#'         | true
        '$share/group1/topic/#'            | true
        '$share/group1//'                  | true
        '$share/group1'                    | false
        '$share/group1/#/filter'           | false
        '$share/group1/++'                 | false
        '$share/group1/##'                 | false
        '$share/group1/topic/filter+'      | false
        '$share/group1/+/+topic/filter'    | false
        '$share/group1/+/#/filter'         | false
        '$share/group1/#/filter'           | false
        '$share/group1/'                   | false
        '$share'                           | false
        '$share//'                         | false
  }
}
