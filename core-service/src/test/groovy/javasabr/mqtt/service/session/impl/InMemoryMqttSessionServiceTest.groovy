package javasabr.mqtt.service.session.impl

import javasabr.mqtt.model.MqttProperties
import javasabr.mqtt.model.message.MqttMessageType
import javasabr.mqtt.model.topic.TopicName
import javasabr.mqtt.network.session.ConfigurableNetworkMqttSession
import javasabr.mqtt.service.IntegrationServiceSpecification
import javasabr.rlib.logger.api.LoggerLevel
import javasabr.rlib.logger.api.LoggerManager

import java.time.Duration

class InMemoryMqttSessionServiceTest extends IntegrationServiceSpecification {
  
  static {
    LoggerManager.enable(InMemoryMessageTacker, LoggerLevel.DEBUG)
  }
  
  def "should create a fresh session"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_1"
        def sessionService = new InMemoryMqttSessionService(60_000, 60_000)
    when:
        def freshSession = fromAsync(sessionService.createClean(testClientId))
    then:
        freshSession != null
        with(freshSession) {
          clientId() == testClientId
          topicNameMapping().size() == 0
          activeSubscriptions().isEmpty()
          incomingProcessingPublishes().size() == 0
          outgoingProcessingPublishes().size() == 0
        }
    cleanup:
        sessionService.close()
  }

  def "should discard old session and create a fresh"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_2"
        def sessionService = new InMemoryMqttSessionService(60_000, 60_000)
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

  def "should discard stored not expirable session and create a fresh"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_2_1"
        def sessionService = new InMemoryMqttSessionService(60_000, 60_000)
        def oldSession = fromAsync(sessionService.createClean(testClientId)) as ConfigurableNetworkMqttSession
        oldSession.expiryInterval(MqttProperties.SESSION_EXPIRY_DURATION_INFINITY)
        def topicNameMappingFromOldSession = oldSession.topicNameMapping()
        topicNameMappingFromOldSession.update(1, TopicName.valueOf("topic/1"))
        topicNameMappingFromOldSession.update(2, TopicName.valueOf("topic/2"))
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
        def sessionService = new InMemoryMqttSessionService(60_000, 60_000)
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
        def sessionService = new InMemoryMqttSessionService(60_000, 60_000)
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

  def "should restore not expirable old session"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_4_1"
        def sessionService = new InMemoryMqttSessionService(60_000, 60_000)
        def oldSession = fromAsync(sessionService.createClean(testClientId)) as ConfigurableNetworkMqttSession
        oldSession.expiryInterval(MqttProperties.SESSION_EXPIRY_DURATION_INFINITY)
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
        !sessionService.storedNotExpirableSessions.containsKey(testClientId)
        with(restoredSession) {
          clientId() == testClientId
          topicNameMapping() == topicNameMappingFromOldSession
          topicNameMapping().size() == 3
        }
    cleanup:
        sessionService.close()
  }

  def "should return nothing when stored session does not exist"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_4_2"
        def sessionService = new InMemoryMqttSessionService(60_000, 60_000)
    when:
        def restoredSession = fromAsync(sessionService.restore(testClientId))
    then:
        restoredSession == null
    cleanup:
        sessionService.close()
  }

  def "should not store not storable session"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_5"
        def sessionService = new InMemoryMqttSessionService(60_000, 60_000)
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
        def sessionService = new InMemoryMqttSessionService(60_000, 60_000)
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

  def "should delete active session and clear state"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_6_1"
        def sessionService = new InMemoryMqttSessionService(60_000, 60_000)
        def activeSession = fromAsync(sessionService.createClean(testClientId)) as ConfigurableNetworkMqttSession
        def topicNameMapping = activeSession.topicNameMapping()
        topicNameMapping.update(1, TopicName.valueOf("topic/1"))
        topicNameMapping.update(2, TopicName.valueOf("topic/2"))
    when:
        def deleteResult = fromAsync(sessionService.delete(testClientId, activeSession))
        def freshSession = fromAsync(sessionService.createClean(testClientId))
    then:
        deleteResult
        freshSession !== activeSession
        topicNameMapping.size() == 0
        freshSession.clientId() == testClientId
        freshSession.topicNameMapping().size() == 0
    cleanup:
        sessionService.close()
  }

  def "should not delete if there is another active session"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_6_2"
        def sessionService = new InMemoryMqttSessionService(60_000, 60_000)
        def testFakeClientId = "InMemoryMqttSessionServiceTest_6_2-fake"
        waitForAsync(sessionService.createClean(testClientId))
        def activeSession = fromAsync(sessionService.createClean(testFakeClientId))
    when:
        waitForAsync(sessionService.delete(testClientId, activeSession))
    then:
        def exception = thrown(IllegalStateException)
        exception.message == "Client:[InMemoryMqttSessionServiceTest_6_2] has another active session"
    cleanup:
        sessionService.close()
  }

  def "should store not expirable session correctly"() {
    given:
        def testClientId = "InMemoryMqttSessionServiceTest_7"
        def sessionService = new InMemoryMqttSessionService(60_000, 60_000)
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
        def sessionService = new InMemoryMqttSessionService(60_000, 60_000)
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
        def sessionService = new InMemoryMqttSessionService(60_000, 60_000)
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

  def "should cleanup expired message meta from active sessions"() {
    given:
        def testClientId1 = "InMemoryMqttSessionServiceTest_10_1"
        def testClientId2 = "InMemoryMqttSessionServiceTest_10_2"
        def sessionService = new InMemoryMqttSessionService(60_000, 50)
        def session1 = fromAsync(sessionService.createClean(testClientId1))
        def session2 = fromAsync(sessionService.createClean(testClientId2))
        def messageTracker1 = session1.inMessageTracker()
        def messageTracker2 = session2.inMessageTracker()
    when:
        messageTracker1.add(11, MqttMessageType.PUBLISH, null, Duration.ofMillis(100))
        messageTracker1.add(12, MqttMessageType.PUBLISH, null, Duration.ofMinutes(100))
        messageTracker2.add(13, MqttMessageType.PUBLISH, null, Duration.ofMinutes(100))
        messageTracker2.add(14, MqttMessageType.PUBLISH, null, Duration.ofMillis(100))
    then:
        waitUntil {
          messageTracker1.stored(11) == null 
              && messageTracker2.stored(14) == null
        }
    then:
        messageTracker1.stored(11) == null
        messageTracker1.stored(12) != null
        messageTracker2.stored(13) != null
        messageTracker2.stored(14) == null
    cleanup:
        sessionService.close()
  }
}
