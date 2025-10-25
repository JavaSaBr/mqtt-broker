package javasabr.mqtt.service.message.out.factory;

import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.reason.code.AuthenticateReasonCode;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode;
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode;
import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.packet.out.ConnectAck311OutPacket;
import javasabr.mqtt.network.packet.out.Disconnect311OutPacket;
import javasabr.mqtt.network.packet.out.MqttWritablePacket;
import javasabr.mqtt.network.packet.out.PingRequest311OutPacket;
import javasabr.mqtt.network.packet.out.PingResponse311OutPacket;
import javasabr.mqtt.network.packet.out.Publish311OutPacket;
import javasabr.mqtt.network.packet.out.PublishAck311OutPacket;
import javasabr.mqtt.network.packet.out.PublishComplete311OutPacket;
import javasabr.mqtt.network.packet.out.PublishOutPacket;
import javasabr.mqtt.network.packet.out.PublishReceived311OutPacket;
import javasabr.mqtt.network.packet.out.PublishRelease311OutPacket;
import javasabr.mqtt.network.packet.out.SubscribeAck311OutPacket;
import javasabr.mqtt.network.packet.out.UnsubscribeAck311OutPacket;
import javasabr.rlib.collections.array.Array;

public class Mqtt311MessageOutFactory extends MqttMessageOutFactory {

  @Override
  public MqttVersion mqttVersion() {
    return MqttVersion.MQTT_3_1_1;
  }

  @Override
  public MqttWritablePacket newConnectAck(
      MqttClient client,
      ConnectAckReasonCode reasonCode,
      boolean sessionPresent,
      String requestedClientId,
      long requestedSessionExpiryInterval,
      int requestedKeepAlive,
      int requestedReceiveMax,
      String reason,
      String serverReference,
      String responseInformation,
      String authenticationMethod,
      byte[] authenticationData,
      Array<StringPair> userProperties) {
    return new ConnectAck311OutPacket(reasonCode, sessionPresent);
  }

  @Override
  public PublishOutPacket newPublish(
      int packetId,
      QoS qos,
      boolean retained,
      boolean duplicate,
      String topicName,
      int topicAlias,
      byte[] payload,
      boolean stringPayload,
      String responseTopic,
      byte[] correlationData,
      Array<StringPair> userProperties) {
    return new Publish311OutPacket(packetId, qos, retained, duplicate, topicName, payload);
  }

  @Override
  public MqttWritablePacket newPublishAck(
      int packetId,
      PublishAckReasonCode reasonCode,
      String reason,
      Array<StringPair> userProperties) {
    return new PublishAck311OutPacket(packetId);
  }

  @Override
  public MqttWritablePacket newSubscribeAck(
      int packetId,
      Array<SubscribeAckReasonCode> reasonCodes,
      String reason,
      Array<StringPair> userProperties) {
    return new SubscribeAck311OutPacket(reasonCodes, packetId);
  }

  @Override
  public MqttWritablePacket newUnsubscribeAck(
      int packetId,
      Array<UnsubscribeAckReasonCode> reasonCodes,
      Array<StringPair> userProperties,
      String reason) {
    return new UnsubscribeAck311OutPacket(packetId);
  }

  @Override
  public MqttWritablePacket newDisconnect(
      MqttClient client,
      DisconnectReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason,
      String serverReference) {
    return new Disconnect311OutPacket();
  }

  @Override
  public MqttWritablePacket newAuthenticate(
      AuthenticateReasonCode reasonCode,
      String authenticateMethod,
      byte[] authenticateData,
      Array<StringPair> userProperties,
      String reason) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MqttWritablePacket newPingRequest() {
    return new PingRequest311OutPacket();
  }

  @Override
  public MqttWritablePacket newPingResponse() {
    return new PingResponse311OutPacket();
  }

  @Override
  public MqttWritablePacket newPublishRelease(
      int packetId,
      PublishReleaseReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason) {
    return new PublishRelease311OutPacket(packetId);
  }

  @Override
  public MqttWritablePacket newPublishReceived(
      int packetId,
      PublishReceivedReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason) {
    return new PublishReceived311OutPacket(packetId);
  }

  @Override
  public MqttWritablePacket newPublishCompleted(
      int packetId,
      PublishCompletedReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason) {
    return new PublishComplete311OutPacket(packetId);
  }
}
