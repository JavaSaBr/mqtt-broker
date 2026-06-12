package javasabr.mqtt.acl.mug.dsl.parser

import javasabr.mqtt.acl.engine.exception.AclConfigurationException
import spock.lang.Specification
import spock.lang.Unroll

class GaclParserTest extends Specification {

  GaclParser parser = new GaclParser()

  @Unroll
  def "should parse string literal containing #pattern without treating it as a comment"() {
    when:
        def rules = parser.parse("""allowPublish {
                      users { userName eq("$pattern") }
                      topics { anyTopic() }
                    }""")

    then:
        rules.size() == 1

    where:
        pattern << ['//admin', 'a/*b']
  }

  def "should parse all user conditions"() {
    when:
        def rules = parser.parse("""allowPublish {
                      users {
                        userName eq("user1")
                        clientId startsWith("dev_")
                        ipAddress regex("192.*")
                      }
                      topics { anyTopic() }
                    }""")
    then:
        rules.size() == 1
  }

  def "should parse complex conditions"() {
    when:
        def rules = parser.parse("""allowPublish {
                      users {
                        anyOf {
                          userName eq("u1")
                          clientId eq("c1")
                        }
                      }
                      topics {
                        eq("test/topic")
                        match("test/#")
                      }
                    }""")
    then:
        rules.size() == 1
  }

  def "should parse all directives"() {
    when:
        def rules = parser.parse("""
                    allowPublish { users { anyUser() } topics { anyTopic() } }
                    denyPublish { users { anyUser() } topics { anyTopic() } }
                    allowSubscribe { users { anyUser() } topics { anyTopic() } }
                    denySubscribe { users { anyUser() } topics { anyTopic() } }
                    """)
    then:
        rules.size() == 4
  }

  def "should throw exception for invalid identity type"() {
    when:
        parser.parse("""allowPublish {
          users { unknown eq("u1") }
          topics { anyTopic() }
        }""")
    then:
        thrown(AclConfigurationException)
  }

  def "should throw exception for invalid topic name"() {
    when:
        parser.parse("""allowPublish {
          users { anyUser() }
          topics { eq("invalid#topic") }
        }""")
    then:
        thrown(AclConfigurationException)
  }

  def "should handle empty users section"() {
    when:
        parser.parse("""allowPublish {
          users {}
          topics { anyTopic() }
        }""")
    then:
        thrown(AclConfigurationException)
  }

  def "should handle empty users section"() {
    when:
        parser.parse("""allowPublish {
          users { userId() }
          topics { anyTopic() }
        }""")
    then:
        thrown(AclConfigurationException)
  }

  def "should handle blank input"() {
    when:
        parser.parse("")
    then:
        thrown(AclConfigurationException)
  }

  def "should trigger line column calculation for multi-line error"() {
    when:
        parser.parse("""allowPublish {
          users { anyUser() }
          topics { anyTopic() }
        }
        invalid""")
    then:
        def e = thrown(AclConfigurationException)
        e.message.contains("line 5, column 9")
  }
}

