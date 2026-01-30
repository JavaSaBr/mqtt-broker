package javasabr.mqtt.service.session.impl

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.network.session.ConfigurableNetworkMqttSession
import javasabr.mqtt.service.IntegrationServiceSpecification

import java.time.Duration

class InMemoryMqttSessionServiceTest extends IntegrationServiceSpecification {
  
  InMemoryMqttSessionService sessionService = new InMemoryMqttSessionService(60_000)
 
  def "should create a fresh session"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_1"
    when:
        def freshSession = fromAsync(sessionService.createClean(testClientId))
    then:
        freshSession != null
        with(freshSession) {
          clientId() == testClientId
          topicNameMapping().size() == 0
          activeSubscriptions().isEmpty()
          inProcessingPublishes().size() == 0
          outProcessingPublishes().size() == 0
        }
    cleanup:
        sessionService.close()
  }

  def "should discard old session and create a fresh"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_2"
        def oldSession = fromAsync(sessionService.createClean(testClientId)) as ConfigurableNetworkMqttSession
        oldSession.expiryInterval(Duration.ofMinutes(5))
        def topicNameMappingFromOldSession = oldSession.topicNameMapping()
        topicNameMappingFromOldSession.update(1, TopicName.valueOf("topic/1"))
        topicNameMappingFromOldSession.update(2, TopicName.valueOf("topic/2"))
        topicNameMappingFromOldSession.update(3, TopicName.valueOf("topic/3"))
        waitForAsync(sessionService.store(testClientId, oldSession))
    when:
        def freshSession = fromAsync(sessionService.createClean(testClientId))
    then:
        freshSession !== oldSession 
        with(freshSession) {
          clientId() == testClientId
          topicNameMapping() !== topicNameMappingFromOldSession
          topicNameMapping().size() == 0
        }
        topicNameMappingFromOldSession.size() == 0
    cleanup:
        sessionService.close()
  }

  def "should not allow to create a new session if we already have some active"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_3"
        def activeSession = fromAsync(sessionService.createClean(testClientId)) as ConfigurableNetworkMqttSession
        activeSession.expiryInterval(Duration.ofMinutes(5))
    when:
        waitForAsync(sessionService.createClean(testClientId))
    then:
        def exception = thrown(IllegalStateException)
        exception.message == "Client:[InMemoryMqttSessionServiceTest_3] already has active session"
    cleanup:
        sessionService.close()
  }

  def "should restore expirable old session"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_4"
        def oldSession = fromAsync(sessionService.createClean(testClientId)) as ConfigurableNetworkMqttSession
        oldSession.expiryInterval(Duration.ofMinutes(5))
        def topicNameMappingFromOldSession = oldSession.topicNameMapping()
        topicNameMappingFromOldSession.update(1, TopicName.valueOf("topic/1"))
        topicNameMappingFromOldSession.update(2, TopicName.valueOf("topic/2"))
        topicNameMappingFromOldSession.update(3, TopicName.valueOf("topic/3"))
    when:
        def storeResult = fromAsync(sessionService.store(testClientId, oldSession))
        def restoredSession = fromAsync(sessionService.restore(testClientId))
    then:
        storeResult
        restoredSession == oldSession
        with(restoredSession) {
          clientId() == testClientId
          topicNameMapping() == topicNameMappingFromOldSession
          topicNameMapping().size() == 3
        }
    cleanup:
        sessionService.close()
  }

  def "should not store not storable session"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_5"
        def sessionToStore = fromAsync(sessionService.createClean(testClientId)) as ConfigurableNetworkMqttSession
        sessionToStore.expiryInterval(MqttProperties.SESSION_EXPIRY_DURATION_DISABLED)
        def topicNameMappingFromOldSession = sessionToStore.topicNameMapping()
        topicNameMappingFromOldSession.update(1, TopicName.valueOf("topic/1"))
        topicNameMappingFromOldSession.update(2, TopicName.valueOf("topic/2"))
        topicNameMappingFromOldSession.update(3, TopicName.valueOf("topic/3"))
    when:
        def storeResult = fromAsync(sessionService.store(testClientId, sessionToStore))
        def restored = fromAsync(sessionService.restore(testClientId))
    then:
        !storeResult
        restored == null
    cleanup:
        sessionService.close()
  }

  def "should not store if there is another active session"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_6"
        def testFakeClientId = "InMemoryMqttSessionServiceTest_6-fake"
        waitForAsync(sessionService.createClean(testClientId))
        def activeSession = fromAsync(sessionService.createClean(testFakeClientId))
    when:
        waitForAsync(sessionService.store(testClientId, activeSession))
    then:
        def exception = thrown(IllegalStateException)
        exception.message == "Client:[InMemoryMqttSessionServiceTest_6] has another active session"
    cleanup:
        sessionService.close()
  }

  def "should store not expirable session correctly"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_7"
        def sessionToStore = fromAsync(sessionService.createClean(testClientId)) as ConfigurableNetworkMqttSession
        sessionToStore.expiryInterval(MqttProperties.SESSION_EXPIRY_DURATION_INFINITY)
    when:
        def storeResult = fromAsync(sessionService.store(testClientId, sessionToStore))
    then:
        storeResult
        sessionService.storedNotExpirableSessions.containsKey(testClientId)
    cleanup:
        sessionService.close()
  }

  def "should not allow to restore the same session twice"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_8"
        def session = fromAsync(sessionService.createClean(testClientId)) as ConfigurableNetworkMqttSession
        session.expiryInterval(MqttProperties.SESSION_EXPIRY_DURATION_INFINITY)
        fromAsync(sessionService.store(testClientId, session))
    when:
        def restored = fromAsync(sessionService.restore(testClientId))
    then:
        restored != null
    when:
        waitForAsync(sessionService.restore(testClientId))
    then:
        def exception = thrown(IllegalStateException)
        exception.message == "Client:[InMemoryMqttSessionServiceTest_8] already has active session"
    cleanup:
        sessionService.close()
  }

  def "should store expirable session correctly"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_9"
        def sessionToStore = fromAsync(sessionService.createClean(testClientId)) as ConfigurableNetworkMqttSession
        sessionToStore.expiryInterval(Duration.ofSeconds(120))
    when:
        def storeResult = fromAsync(sessionService.store(testClientId, sessionToStore))
    then:
        storeResult
        sessionService.storedExpirableSessions.containsKey(testClientId)
    cleanup:
        sessionService.close()
  }
}
