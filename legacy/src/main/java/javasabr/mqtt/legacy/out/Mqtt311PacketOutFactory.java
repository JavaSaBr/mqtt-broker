package javasabr.mqtt.legacy.out;

import javasabr.mqtt.legacy.model.QoS;
import javasabr.mqtt.legacy.model.data.type.StringPair;
import javasabr.mqtt.legacy.model.reason.code.AuthenticateReasonCode;
import javasabr.mqtt.legacy.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.legacy.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.legacy.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.legacy.model.reason.code.PublishCompletedReasonCode;
import javasabr.mqtt.legacy.model.reason.code.PublishReceivedReasonCode;
import javasabr.mqtt.legacy.model.reason.code.PublishReleaseReasonCode;
import javasabr.mqtt.legacy.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.legacy.model.reason.code.UnsubscribeAckReasonCode;
import javasabr.mqtt.legacy.network.client.MqttClient;
import javasabr.mqtt.legacy.network.packet.out.ConnectAck311OutPacket;
import javasabr.mqtt.legacy.network.packet.out.Disconnect311OutPacket;
import javasabr.mqtt.legacy.network.packet.out.MqttWritablePacket;
import javasabr.mqtt.legacy.network.packet.out.PingRequest311OutPacket;
import javasabr.mqtt.legacy.network.packet.out.PingResponse311OutPacket;
import javasabr.mqtt.legacy.network.packet.out.Publish311OutPacket;
import javasabr.mqtt.legacy.network.packet.out.PublishAck311OutPacket;
import javasabr.mqtt.legacy.network.packet.out.PublishComplete311OutPacket;
import javasabr.mqtt.legacy.network.packet.out.PublishOutPacket;
import javasabr.mqtt.legacy.network.packet.out.PublishReceived311OutPacket;
import javasabr.mqtt.legacy.network.packet.out.PublishRelease311OutPacket;
import javasabr.mqtt.legacy.network.packet.out.SubscribeAck311OutPacket;
import javasabr.mqtt.legacy.network.packet.out.UnsubscribeAck311OutPacket;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.MutableArray;

public class Mqtt311PacketOutFactory extends MqttPacketOutFactory {

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
      MutableArray<StringPair> userProperties) {
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
      MutableArray<StringPair> userProperties) {
    return new Publish311OutPacket(packetId, qos, retained, duplicate, topicName, payload);
  }

  @Override
  public MqttWritablePacket newPublishAck(
      int packetId,
      PublishAckReasonCode reasonCode,
      String reason,
      MutableArray<StringPair> userProperties) {
    return new PublishAck311OutPacket(packetId);
  }

  @Override
  public MqttWritablePacket newSubscribeAck(
      int packetId,
      Array<SubscribeAckReasonCode> reasonCodes,
      String reason,
      MutableArray<StringPair> userProperties) {
    return new SubscribeAck311OutPacket(reasonCodes, packetId);
  }

  @Override
  public MqttWritablePacket newUnsubscribeAck(
      int packetId,
      Array<UnsubscribeAckReasonCode> reasonCodes,
      MutableArray<StringPair> userProperties,
      String reason) {
    return new UnsubscribeAck311OutPacket(packetId);
  }

  @Override
  public MqttWritablePacket newDisconnect(
      MqttClient client,
      DisconnectReasonCode reasonCode,
      MutableArray<StringPair> userProperties,
      String reason,
      String serverReference) {
    return new Disconnect311OutPacket();
  }

  @Override
  public MqttWritablePacket newAuthenticate(
      AuthenticateReasonCode reasonCode,
      String authenticateMethod,
      byte[] authenticateData,
      MutableArray<StringPair> userProperties,
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
      MutableArray<StringPair> userProperties,
      String reason) {
    return new PublishRelease311OutPacket(packetId);
  }

  @Override
  public MqttWritablePacket newPublishReceived(
      int packetId,
      PublishReceivedReasonCode reasonCode,
      MutableArray<StringPair> userProperties,
      String reason) {
    return new PublishReceived311OutPacket(packetId);
  }

  @Override
  public MqttWritablePacket newPublishCompleted(
      int packetId,
      PublishCompletedReasonCode reasonCode,
      MutableArray<StringPair> userProperties,
      String reason) {
    return new PublishComplete311OutPacket(packetId);
  }
}
