package javasabr.mqtt.service.acl

import java.security.SecureRandom

import static java.lang.System.currentTimeMillis

class TestRulesGenerator {

  private static final SecureRandom RANDOM = new SecureRandom()

  static def generateIP(def base) {
    def octet3 = (base - 1) / 256
    def octet4 = (base - 1) % 256
    return "10.${octet3}.${octet4}.${(base % 10) + 1}"
  }

  static File generate(int ruleCount) {
    def fileName = "${RANDOM.nextLong()}.groovy"
    def file = new File("build/${fileName}")
    file.withWriter('UTF-8') { writer ->
      def start = currentTimeMillis()
      1.upto(ruleCount) { ruleNum ->
        def nextI = ruleNum + 1
        def ip1 = generateIP(ruleNum)
        def ip2 = generateIP(ruleNum + 7)
        def ruleType = ruleNum % 4

        def ruleContent = ""

        if (ruleType == 1) {
          ruleContent = """
allowPublish {
  anyOf {
    userName eq("user_${ruleNum}"), regex("user_${ruleNum}\\\$")
    clientId eq("client_${ruleNum}"), regex("^client_${ruleNum}")
    ipAddress eq("${ip1}"), eq("${ip2}")
    allOf {
      userName eq("user_${nextI}")
      clientId eq("client_${nextI}")
      ipAddress eq("${generateIP(nextI)}")
    }
  }
  topicName eq("/data/temp/${ruleNum}"), eq("/status/log/${ruleNum}")
}
"""
        } else if (ruleType == 2) {
          ruleContent = """
denySubscribe {
  allOf {
    userName eq("user_${ruleNum}")
    userName regex("user_${ruleNum}\\\$")
    clientId eq("client_${ruleNum}")
    clientId regex("^client_${ruleNum}")
    ipAddress eq("${ip1}")
    ipAddress eq("${ip2}")
  }
  topicFilter match("/config/+/ ${ruleNum}"), match("/control/#")
}
"""
        } else if (ruleType == 3) {
          ruleContent = """
allowSubscribe {
  anyOf {
    userName eq("user_${ruleNum}"), regex("user_${ruleNum}\\\$")
    clientId eq("client_${ruleNum}"), regex("^client_${ruleNum}")
    ipAddress eq("${ip1}"), eq("${ip2}")
  }
  topicFilter match("/sensor/temp/${ruleNum}"), match("/sensor/+/log")
}
"""
        } else if (ruleType == 0) {
          ruleContent = """
denyPublish {
  allOf {
    userName eq("user_${ruleNum}")
    clientId eq("client_${ruleNum}")
    ipAddress eq("${ip1}")
  }
  topicName eq("/admin/alerts/${ruleNum}"), eq("/system/update/${ruleNum}")
}
"""
        }
        writer.write(ruleContent)
      }
      println "Rules file ${fileName} with ${ruleCount} rules was generated in ${currentTimeMillis() - start} ms"
    }
    return file
  }
}
