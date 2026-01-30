package javasabr.mqtt.service.session.impl

import javasabr.mqtt.test.support.UnitSpecification
import javasabr.rlib.collections.array.MutableArray
import javasabr.rlib.collections.dictionary.DictionaryFactory

import java.util.concurrent.ThreadLocalRandom

class OldestSessionCleanerTest extends UnitSpecification {

  def "should remove the oldest 20 sessions"() {
    given:
        def allSessions = DictionaryFactory
            .stampedLockBasedRefToRefDictionary(String, NotExpirableSession)
        def cleaner = new OldestSessionCleaner<NotExpirableSession>(allSessions, 50, 10)
        def random = ThreadLocalRandom.current()
        def shouldBeRemoved = MutableArray.ofType(String)
        60.times {
          def session = new InMemoryNetworkMqttSession("session_${random.nextInt()}_$it")
          def notExpirableSession = new NotExpirableSession(it + 1, session)
          allSessions.put(session.clientId(), notExpirableSession)
          if (it < 20) {
            shouldBeRemoved.add(session.clientId())
          }
        }
    when:
        cleaner.cleanup()
    then:
        allSessions.size() == 40
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

  def "should remove only the 11 oldest sessions"() {
    given:
        def allSessions = DictionaryFactory
            .stampedLockBasedRefToRefDictionary(String, NotExpirableSession)
        def cleaner = new OldestSessionCleaner<NotExpirableSession>(allSessions, 50, 10)
        def random = ThreadLocalRandom.current()
        def shouldBeRemoved = MutableArray.ofType(String)
        51.times {
          def session = new InMemoryNetworkMqttSession("session_${random.nextInt()}_$it")
          def notExpirableSession = new NotExpirableSession(it + 1, session)
          allSessions.put(session.clientId(), notExpirableSession)
          if (it < 10) {
            shouldBeRemoved.add(session.clientId())
          }
        }
    when:
        cleaner.cleanup()
    then:
        allSessions.size() == 40
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

  def "should not remove any sessions"() {
    given:
        def allSessions = DictionaryFactory
            .stampedLockBasedRefToRefDictionary(String, NotExpirableSession)
        def cleaner = new OldestSessionCleaner<NotExpirableSession>(allSessions, 50, 10)
        def random = ThreadLocalRandom.current()
        50.times {
          def session = new InMemoryNetworkMqttSession("session_${random.nextInt()}_$it")
          def notExpirableSession = new NotExpirableSession(it + 1, session)
          allSessions.put(session.clientId(), notExpirableSession)
        }
    when:
        cleaner.cleanup()
    then:
        allSessions.size() == 50
  }

  def "should remove only the 11 oldest sessions even when all have the same storedAt"() {
    given:
        def allSessions = DictionaryFactory
            .stampedLockBasedRefToRefDictionary(String, NotExpirableSession)
        def cleaner = new OldestSessionCleaner<NotExpirableSession>(allSessions, 50, 10)
        def random = ThreadLocalRandom.current()
        51.times {
          def session = new InMemoryNetworkMqttSession("session_${random.nextInt()}_$it")
          def notExpirableSession = new NotExpirableSession(5, session)
          allSessions.put(session.clientId(), notExpirableSession)
        }
    when:
        cleaner.cleanup()
    then:
        allSessions.size() == 40
  }
}
