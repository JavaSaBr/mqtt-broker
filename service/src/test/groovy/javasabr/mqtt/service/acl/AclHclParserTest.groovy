package javasabr.mqtt.service.acl

import javasabr.mqtt.test.support.UnitSpecification

class AclHclParserTest extends UnitSpecification {

  def "should parse HCL-file"() {
    given:
        def aclConfigFile = "acl.hcl";
    when:
        def parsedConfig = AclHclParser.parse(aclConfigFile)
    then:
        parsedConfig.get("acl") != null
        parsedConfig.get("acl").get("version") == 1.0

        parsedConfig.get("user") != null
        parsedConfig.get("user").get("dashboard") != null
        parsedConfig.get("user").get("sensor1") != null

        parsedConfig.get("group") != null
        parsedConfig.get("group").get("admins") != null
        parsedConfig.get("group").get("sensors") != null

        parsedConfig.get("rule") != null
        parsedConfig.get("rule").get("sys_dashboard_sub") != null
        parsedConfig.get("rule").get("sys_dashboard_sub_2") != null
        parsedConfig.get("rule").get("pub_temperature") != null
        parsedConfig.get("rule").get("ip_pubsub") != null
        parsedConfig.get("rule").get("deny_all_sys_and_hash_sub") != null
  }
}
