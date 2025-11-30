package javasabr.mqtt.model.acl.matcher

import javasabr.mqtt.model.topic.AbstractTopic
import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.test.support.UnitSpecification

import java.util.regex.Pattern

class MatcherTest extends UnitSpecification {

  def "should match topic filter"(AbstractTopic topicFilter, AbstractTopic incomingValue, boolean expectedResult) {
    given:
        def matcher = new TopicFilterMatcher(topicFilter)
    when:
        boolean result = matcher.test(incomingValue)
    then:
        result == expectedResult
    where:
        incomingValue                                               | topicFilter                                   | expectedResult
        TopicFilter.valueOf("topic1")                               | TopicFilter.valueOf("#")                      | true
        TopicFilter.valueOf("sport")                                | TopicFilter.valueOf("sport/#")                | true
        TopicFilter.valueOf("sport/tennis/player1")                 | TopicFilter.valueOf("sport/tennis/player1/#") | true
        TopicFilter.valueOf("sport/tennis/player1/ranking")         | TopicFilter.valueOf("sport/tennis/player1/#") | true
        TopicFilter.valueOf("sport/tennis/player1/score/wimbledon") | TopicFilter.valueOf("sport/tennis/player1/#") | true
        TopicFilter.valueOf("/finance")                             | TopicFilter.valueOf("+/+")                    | true
        TopicFilter.valueOf("/finance")                             | TopicFilter.valueOf("/+")                     | true
        TopicFilter.valueOf("/finance")                             | TopicFilter.valueOf("+")                      | false
        TopicFilter.valueOf("finance")                              | TopicFilter.valueOf("+")                      | true
        TopicFilter.valueOf("finance/stocks")                       | TopicFilter.valueOf("+/stocks")               | true
        TopicFilter.valueOf("finance/stocks")                       | TopicFilter.valueOf("finance/+")              | true
        TopicFilter.valueOf("finance/stocks/value")                 | TopicFilter.valueOf("finance/+")              | false
        TopicFilter.valueOf("a/b/c")                                | TopicFilter.valueOf("a/#")                    | true
        TopicFilter.valueOf("a/b/c")                                | TopicFilter.valueOf("#")                      | true
        TopicFilter.valueOf("")                                     | TopicFilter.valueOf("#")                      | true
        TopicFilter.valueOf("a")                                    | TopicFilter.valueOf("a/#")                    | true
        TopicFilter.valueOf("a/")                                   | TopicFilter.valueOf("a/#")                    | true
        TopicFilter.valueOf("sport/tennis/player1")                 | TopicFilter.valueOf("sport/+/player1")        | true
        TopicFilter.valueOf("sport/tennis/player1")                 | TopicFilter.valueOf("sport/+/player2")        | false
        TopicFilter.valueOf("sport/tennis/")                        | TopicFilter.valueOf("sport/+/+")              | true
        TopicFilter.valueOf("sport//player1")                       | TopicFilter.valueOf("sport/+/player1")        | true
        TopicFilter.valueOf("sport//player1")                       | TopicFilter.valueOf("sport/+/+")              | true
        TopicFilter.valueOf("sport///")                             | TopicFilter.valueOf("sport/+/+/#")            | true
        TopicFilter.valueOf("///")                                  | TopicFilter.valueOf("+/+/+/+")                | true
        TopicFilter.valueOf("///")                                  | TopicFilter.valueOf("+/+/+")                  | false
        TopicFilter.valueOf("///")                                  | TopicFilter.valueOf("+/+")                    | false
        TopicFilter.valueOf("sport/")                               | TopicFilter.valueOf("sport/+")                | true
        TopicFilter.valueOf("sport/")                               | TopicFilter.valueOf("sport")                  | false
        TopicFilter.valueOf("sport/")                               | TopicFilter.valueOf("sport/#")                | true
        TopicFilter.valueOf("a/b")                                  | TopicFilter.valueOf("a/b/c")                  | false
        TopicFilter.valueOf("a/b/c")                                | TopicFilter.valueOf("a/b")                    | false
        TopicFilter.valueOf("a")                                    | TopicFilter.valueOf("+/+")                    | false
        TopicFilter.valueOf("a/b/c")                                | TopicFilter.valueOf("a/b/c")                  | true
        TopicFilter.valueOf("a/b/c")                                | TopicFilter.valueOf("a/b/d")                  | false
        TopicFilter.valueOf("a")                                    | TopicFilter.valueOf("a/")                     | false
        TopicFilter.valueOf("")                                     | TopicFilter.valueOf("+")                      | true
        TopicFilter.valueOf("")                                     | TopicFilter.valueOf("/+")                     | false
        TopicFilter.valueOf("/")                                    | TopicFilter.valueOf("/")                      | true
        TopicFilter.valueOf("/")                                    | TopicFilter.valueOf("+")                      | false
        TopicFilter.valueOf("/")                                    | TopicFilter.valueOf("+/+")                    | true
        TopicFilter.valueOf("a/b/c")                                | TopicFilter.valueOf("a/#/b")                  | false
        TopicFilter.valueOf("a/b")                                  | TopicFilter.valueOf("aa/b")                   | false
  }

  def "should match topic filter"(String pattern, String incomingValue, boolean expectedResult) {
    given:
        def matcher = new RegexMatcher(Pattern.compile(pattern))
    when:
        boolean result = matcher.test(incomingValue)
    then:
        result == expectedResult
    where:
        pattern     | incomingValue | expectedResult
        "^sensor\$" | "sensor"      | true
        "^sensor\$" | "/sensor"     | false
        "^sensor\$" | "sensor1"     | false
  }
}
