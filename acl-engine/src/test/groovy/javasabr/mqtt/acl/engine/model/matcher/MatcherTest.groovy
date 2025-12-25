package javasabr.mqtt.acl.engine.model.matcher

import javasabr.mqtt.model.topic.TopicFilter
import javasabr.mqtt.model.topic.TopicValidator
import javasabr.mqtt.test.support.UnitSpecification

import java.util.regex.Pattern

class MatcherTest extends UnitSpecification {

  def "should match topic filter"(String topicFilter, String incomingValue, boolean expectedResult) {
    given:
        def matcher = new TopicFilterMatcher(TopicFilter.valueOf(topicFilter))
    when:
        boolean result = matcher.test(TopicFilter.valueOf(incomingValue))
    then:
        result == expectedResult
    where:
        incomingValue                  | topicFilter              | expectedResult
        "topic1"                       | "#"                      | true
        "sport"                        | "sport/#"                | true
        "sport/tennis/player1"         | "sport/tennis/player1/#" | true
        "sport/tennis/player1/ranking" | "sport/tennis/player1/#" | true
        "sport/tennis/player1/score/a" | "sport/tennis/player1/#" | true
        "/finance"                     | "+/+"                    | true
        "/finance"                     | "/+"                     | true
        "/finance"                     | "+"                      | false
        "finance"                      | "+"                      | true
        "finance/stocks"               | "+/stocks"               | true
        "finance/stocks"               | "finance/+"              | true
        "finance/stocks/value"         | "finance/+"              | false
        "a/b/c"                        | "a/#"                    | true
        "a/b/c"                        | "#"                      | true
        ""                             | "#"                      | true
        "a"                            | "a/#"                    | true
        "a/"                           | "a/#"                    | true
        "sport/tennis/player1"         | "sport/+/player1"        | true
        "sport/tennis/player1"         | "sport/+/player2"        | false
        "sport/tennis/"                | "sport/+/+"              | true
        "sport//player1"               | "sport/+/player1"        | true
        "sport//player1"               | "sport/+/+"              | true
        "sport///"                     | "sport/+/+/#"            | true
        "///"                          | "+/+/+/+"                | true
        "///"                          | "+/+/+"                  | false
        "///"                          | "+/+"                    | false
        "sport/"                       | "sport/+"                | true
        "sport/"                       | "sport"                  | false
        "sport/"                       | "sport/#"                | true
        "a/b"                          | "a/b/c"                  | false
        "a/b/c"                        | "a/b"                    | false
        "a"                            | "+/+"                    | false
        "a/b/c"                        | "a/b/c"                  | true
        "a/b/c"                        | "a/b/d"                  | false
        "a"                            | "a/"                     | false
        ""                             | "+"                      | true
        ""                             | "/+"                     | false
        "/"                            | "/"                      | true
        "/"                            | "+"                      | false
        "/"                            | "+/+"                    | true
        "a/b"                          | "aa/b"                   | false
  }

  def "should match string pattern matcher"(String pattern, String incomingValue, boolean expectedResult) {
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

  def "should match prefix matcher"(String prefix, String incomingValue, boolean expectedResult) {
    given:
        def matcher = new StartsWithMatcher(prefix)
    when:
        boolean result = matcher.test(incomingValue)
    then:
        result == expectedResult
    where:
        prefix    | incomingValue | expectedResult
        "client_" | "client_123"  | true
        "client_" | "123_client"  | false
        "client_" | "_cli"        | false
  }

  def "should match contains matcher"(String substring, String incomingValue, boolean expectedResult) {
    given:
        def matcher = new ContainsMatcher(substring)
    when:
        boolean result = matcher.test(incomingValue)
    then:
        result == expectedResult
    where:
        substring | incomingValue | expectedResult
        "123"     | "client_123"  | true
        "123"     | "client_"     | false
        "123"     | "123_client_" | true
  }
}
