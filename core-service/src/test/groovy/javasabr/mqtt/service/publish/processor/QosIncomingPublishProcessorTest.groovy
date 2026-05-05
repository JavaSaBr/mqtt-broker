package javasabr.mqtt.service.publish.processor

import javasabr.mqtt.service.IntegrationServiceSpecification
import javasabr.rlib.logger.api.LoggerLevel
import javasabr.rlib.logger.api.LoggerManager

abstract class QosIncomingPublishProcessorTest extends IntegrationServiceSpecification {
  static {
    LoggerManager.enable(AbstractIncomingPublishProcessor.class, LoggerLevel.DEBUG)
    LoggerManager.enable(TrackableIncomingPublishProcessor.class, LoggerLevel.DEBUG)
    LoggerManager.enable(Qos0IncomingPublishProcessor.class, LoggerLevel.DEBUG)
    LoggerManager.enable(Qos1IncomingPublishProcessor.class, LoggerLevel.DEBUG)
    LoggerManager.enable(Qos2IncomingPublishProcessor.class, LoggerLevel.DEBUG)
  }
}
