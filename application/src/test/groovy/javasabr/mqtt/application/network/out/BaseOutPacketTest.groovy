package javasabr.mqtt.application.network.out

import javasabr.mqtt.network.MqttClient
import javasabr.mqtt.application.network.NetworkUnitSpecification
import spock.lang.Shared

class BaseOutPacketTest extends NetworkUnitSpecification {

  @Shared
  MqttClient mqtt5Client = Stub(MqttClient.UnsafeMqttClient) {
    connectionConfig() >> mqttConnectionConfig
    sessionExpiryInterval() >> NetworkUnitSpecification.sessionExpiryInterval
    receiveMax() >> NetworkUnitSpecification.receiveMaximum
    maximumPacketSize() >> NetworkUnitSpecification.maximumPacketSize
    clientId() >> clientId
    keepAlive() >> serverKeepAlive
    topicAliasMaximum() >> NetworkUnitSpecification.topicAliasMaximum
  }

  @Shared
  MqttClient mqtt311Client = Stub(MqttClient.UnsafeMqttClient) {
    connectionConfig() >> mqttConnectionConfig
    sessionExpiryInterval() >> NetworkUnitSpecification.sessionExpiryInterval
    receiveMax() >> NetworkUnitSpecification.receiveMaximum
    maximumPacketSize() >> NetworkUnitSpecification.maximumPacketSize
    clientId() >> clientId
    keepAlive() >> serverKeepAlive
    topicAliasMaximum() >> NetworkUnitSpecification.topicAliasMaximum
  }
}
