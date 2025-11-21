package javasabr.mqtt.service.acl

import javasabr.mqtt.model.acl.AclRoot
import javasabr.mqtt.test.support.UnitSpecification

class AclHclParserTest extends UnitSpecification {

  def "should parse Groovy DSL config"() {
    given:
        def aclConfigFile = "acl.groovy";
    when:
        AclRoot root = AclDslMapper.load(aclConfigFile)
    then:
        root != null
        root.acl() != null
        root.acl().version() == 1

        root.user() != null
        root.user().size() == 2
        root.user()[0].name() == "dashboard"
        root.user()[0].groups().size() == 2
        root.user()[0].groups()[0] == "admin"
        root.user()[0].groups()[1] == "viewer"
        root.user()[1].name() == "sensor1"

        root.group() != null
        root.group().size() == 3
        root.group()[0].name() == "admin"
        root.group()[1].name() == "viewer"
        root.group()[2].name() == "sensor"

        root.rule() != null
        root.rule().size() == 2
        root.rule()[0].name() == "sys_dashboard_sub"
        root.rule()[1].name() == "deny_all"
  }
}
