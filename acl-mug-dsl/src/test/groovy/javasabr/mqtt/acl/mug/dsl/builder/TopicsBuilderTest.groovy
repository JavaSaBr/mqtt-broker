package javasabr.mqtt.acl.mug.dsl.builder

import javasabr.mqtt.acl.engine.exception.AclConfigurationException
import spock.lang.Specification

class TopicsBuilderTest extends Specification {

  def "should preserve original cause when dynamic() rejects invalid topic"() {
    when:
        new TopicsBuilder().dynamic("")

    then:
        def e = thrown(AclConfigurationException)
        e.cause instanceof IllegalArgumentException
  }
}
