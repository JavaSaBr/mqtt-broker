package javasabr.mqtt.network.out;

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
import javasabr.mqtt.network.packet.out.Authentication5OutPacket;
import javasabr.mqtt.network.packet.out.ConnectAck5OutPacket;
import javasabr.mqtt.network.packet.out.Disconnect5OutPacket;
import javasabr.mqtt.network.packet.out.MqttWritablePacket;
import javasabr.mqtt.network.packet.out.Publish5OutPacket;
import javasabr.mqtt.network.packet.out.PublishAck5OutPacket;
import javasabr.mqtt.network.packet.out.PublishComplete5OutPacket;
import javasabr.mqtt.network.packet.out.PublishOutPacket;
import javasabr.mqtt.network.packet.out.PublishReceived5OutPacket;
import javasabr.mqtt.network.packet.out.PublishRelease5OutPacket;
import javasabr.mqtt.network.packet.out.SubscribeAck5OutPacket;
import javasabr.mqtt.network.packet.out.UnsubscribeAck5OutPacket;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.MutableArray;

public class Mqtt5PacketOutFactory extends Mqtt311PacketOutFactory {

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
    var config = client.connectionConfig();
    return new ConnectAck5OutPacket(
        reasonCode,
        sessionPresent,
        requestedClientId,
        requestedSessionExpiryInterval,
        requestedKeepAlive,
        requestedReceiveMax,
        reason,
        serverReference,
        responseInformation,
        authenticationMethod,
        authenticationData,
        userProperties,
        client.clientId(),
        config.getMaxQos(),
        client.sessionExpiryInterval(),
        client.maximumPacketSize(),
        client.receiveMax(),
        client.topicAliasMaximum(),
        client.keepAlive(),
        config.isRetainAvailable(),
        config.isWildcardSubscriptionAvailable(),
        config.isSubscriptionIdAvailable(),
        config.isSharedSubscriptionAvailable());
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
    return new Publish5OutPacket(
        packetId,
        qos,
        retained,
        duplicate,
        topicName,
        payload,
        topicAlias,
        stringPayload,
        responseTopic,
        correlationData,
        userProperties);
  }

  @Override
  public MqttWritablePacket newPublishAck(
      int packetId,
      PublishAckReasonCode reasonCode,
      String reason,
      MutableArray<StringPair> userProperties) {
    return new PublishAck5OutPacket(packetId, reasonCode, userProperties, reason);
  }

  @Override
  public MqttWritablePacket newSubscribeAck(
      int packetId,
      Array<SubscribeAckReasonCode> reasonCodes,
      String reason,
      MutableArray<StringPair> userProperties) {
    return new SubscribeAck5OutPacket(packetId, reasonCodes, userProperties, reason);
  }

  @Override
  public MqttWritablePacket newUnsubscribeAck(
      int packetId,
      Array<UnsubscribeAckReasonCode> reasonCodes,
      MutableArray<StringPair> userProperties,
      String reason) {
    return new UnsubscribeAck5OutPacket(packetId, reasonCodes, userProperties, reason);
  }

  @Override
  public MqttWritablePacket newDisconnect(
      MqttClient client,
      DisconnectReasonCode reasonCode,
      MutableArray<StringPair> userProperties,
      String reason,
      String serverReference) {
    return new Disconnect5OutPacket(
        reasonCode,
        userProperties,
        reason,
        serverReference,
        client.sessionExpiryInterval());
  }

  @Override
  public MqttWritablePacket newAuthenticate(
      AuthenticateReasonCode reasonCode,
      String authenticateMethod,
      byte[] authenticateData,
      MutableArray<StringPair> userProperties,
      String reason) {
    return new Authentication5OutPacket(userProperties, reasonCode, reason, authenticateMethod, authenticateData);
  }

  @Override
  public MqttWritablePacket newPublishRelease(
      int packetId,
      PublishReleaseReasonCode reasonCode,
      MutableArray<StringPair> userProperties,
      String reason) {
    return new PublishRelease5OutPacket(packetId, reasonCode, userProperties, reason);
  }

  @Override
  public MqttWritablePacket newPublishReceived(
      int packetId,
      PublishReceivedReasonCode reasonCode,
      MutableArray<StringPair> userProperties,
      String reason) {
    return new PublishReceived5OutPacket(packetId, reasonCode, userProperties, reason);
  }

  @Override
  public MqttWritablePacket newPublishCompleted(
      int packetId,
      PublishCompletedReasonCode reasonCode,
      MutableArray<StringPair> userProperties,
      String reason) {
    return new PublishComplete5OutPacket(packetId, reasonCode, userProperties, reason);
  }
}
