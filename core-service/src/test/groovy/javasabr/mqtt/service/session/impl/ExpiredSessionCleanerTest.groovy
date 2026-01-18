package javasabr.mqtt.service.session.impl

import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.MutableArray
import javasabr.rlib.collections.dictionary.DictionaryFactory

import java.util.concurrent.ThreadLocalRandom

class ExpiredSessionCleanerTest extends UnitSpecification {

  def "should cleanup 30 expired sessions"() {
    given:
        def allSessions = DictionaryFactory
            .stampedLockBasedRefToRefDictionary(String, ExpirableSession)
        def cleaner = new ExpiredSessionCleaner(allSessions)
        def random = ThreadLocalRandom.current()
        def shouldBeRemoved = MutableArray.ofType(String)
        def currentTime = System.currentTimeMillis()
        60.times {
          def expiredAt = it < 30 ? 1 : currentTime + 10_000;
          def session = new InMemoryNetworkMqttSession("session_${random.nextInt()}_$it")
          def expirableSession = new ExpirableSession(it + 1, session, expiredAt)
          allSessions.put(session.clientId(), expirableSession)
          if (it < 30) {
            shouldBeRemoved.add(session.clientId())
          }
        }
    when:
        cleaner.cleanup()
    then:
        allSessions.size() == 30
    when:
        int exists = 0
        for (def clientId in shouldBeRemoved) {
          if (allSessions.containsKey(clientId)) {
            exists++
          }
        }
    then:
        exists == 0
  }
}
