package javasabr.mqtt.service.acl

import java.security.SecureRandom

class TestRulesGenerator {

  private static final SecureRandom RANDOM = new SecureRandom()

  static File generate(int ruleCount) {
    def fileName = "${RANDOM.nextLong()}.groovy"
    def file = new File("build/${fileName}")
    file.withWriter('UTF-8') { writer ->
      1.upto(ruleCount) { ruleNum ->
        def nextNum = ruleNum + 1
        def ip1 = "10.${ruleNum}.${ruleNum}.${ruleNum}"
        def ip2 = "10.${ruleNum + 7}.${ruleNum + 7}.${ruleNum + 7}"
        def ruleContent = switch (ruleNum % 4) {
          case 1 -> """
                  allowPublish {
                    anyOf {
                      userName eq("user_${ruleNum}"), regex("user_${ruleNum}\\\$")
                      clientId eq("client_${ruleNum}"), regex("^client_${ruleNum}")
                      ipAddress eq("${ip1}"), eq("${ip2}")
                      allOf {
                        userName eq("user_${nextNum}")
                        clientId eq("client_${nextNum}")
                        ipAddress eq("10.${nextNum}.${nextNum}.${nextNum}")
                      }
                    }
                    topicName eq("/data/temp/${ruleNum}"), eq("/status/log/${ruleNum}")
                  }
                  """
          case 2 -> """
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
          case 3 -> """
                  allowSubscribe {
                    anyOf {
                      userName eq("user_${ruleNum}"), regex("user_${ruleNum}\\\$")
                      clientId eq("client_${ruleNum}"), regex("^client_${ruleNum}")
                      ipAddress eq("${ip1}"), eq("${ip2}")
                    }
                    topicFilter match("/sensor/temp/${ruleNum}"), match("/sensor/+/log")
                  }
                  """
          default -> """
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
    }
    return file
  }
}
