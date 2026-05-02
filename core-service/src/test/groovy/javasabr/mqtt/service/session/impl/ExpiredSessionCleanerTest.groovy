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
          def session = new InMemoryNetworkMqttSession("session_${random.nextInt()}_$it", 1)
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

  def "should not cleanup any expired sessions"() {
    given:
        def allSessions = DictionaryFactory
            .stampedLockBasedRefToRefDictionary(String, ExpirableSession)
        def cleaner = new ExpiredSessionCleaner(allSessions)
        def random = ThreadLocalRandom.current()
        def currentTime = System.currentTimeMillis()
        60.times {
          def session = new InMemoryNetworkMqttSession("session_${random.nextInt()}_$it", 2)
          def expirableSession = new ExpirableSession(it + 1, session, currentTime + 10_000)
          allSessions.put(session.clientId(), expirableSession)
        }
    when:
        cleaner.cleanup()
    then:
        allSessions.size() == 60
  }
}
