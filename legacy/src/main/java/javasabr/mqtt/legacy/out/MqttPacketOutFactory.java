package javasabr.mqtt.legacy.out;

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
import javasabr.mqtt.model.network.MqttClient;
import javasabr.mqtt.legacy.network.packet.out.MqttWritablePacket;
import javasabr.mqtt.legacy.network.packet.out.PublishOutPacket;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.common.util.StringUtils;

public abstract class MqttPacketOutFactory {

  public abstract MqttWritablePacket newConnectAck(
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
      MutableArray<StringPair> userProperties);

  public MqttWritablePacket newConnectAck(
      MqttClient client,
      ConnectAckReasonCode reasonCode,
      boolean sessionPresent,
      String requestedClientId,
      long requestedSessionExpiryInterval,
      int requestedKeepAlive,
      int requestedReceiveMax) {
    return newConnectAck(
        client,
        reasonCode,
        sessionPresent,
        requestedClientId,
        requestedSessionExpiryInterval,
        requestedKeepAlive,
        requestedReceiveMax,
        StringUtils.EMPTY,
        StringUtils.EMPTY,
        StringUtils.EMPTY,
        StringUtils.EMPTY,
        ArrayUtils.EMPTY_BYTE_ARRAY,
        MutableArray.ofType(StringPair.class));
  }

  public MqttWritablePacket newConnectAck(MqttClient client, ConnectAckReasonCode reasonCode) {
    return newConnectAck(
        client,
        reasonCode,
        false,
        StringUtils.EMPTY,
        client.getSessionExpiryInterval(),
        client.getKeepAlive(),
        client.getReceiveMax(),
        StringUtils.EMPTY,
        StringUtils.EMPTY,
        StringUtils.EMPTY,
        StringUtils.EMPTY,
        ArrayUtils.EMPTY_BYTE_ARRAY,
        MutableArray.ofType(StringPair.class));
  }

  public PublishOutPacket newPublish(
      int packetId,
      QoS qos,
      boolean retained,
      boolean duplicate,
      String topicName,
      byte[] payload) {
    return newPublish(
        packetId,
        qos,
        retained,
        duplicate,
        topicName,
        0,
        payload,
        false,
        StringUtils.EMPTY,
        ArrayUtils.EMPTY_BYTE_ARRAY,
        MutableArray.ofType(StringPair.class));
  }

  public abstract PublishOutPacket newPublish(
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
      MutableArray<StringPair> userProperties);

  public abstract MqttWritablePacket newPublishAck(
      int packetId,
      PublishAckReasonCode reasonCode,
      String reason,
      MutableArray<StringPair> userProperties);

  public MqttWritablePacket newPublishAck(int packetId, PublishAckReasonCode reasonCode) {
    return newPublishAck(packetId, reasonCode, StringUtils.EMPTY, MutableArray.ofType(StringPair.class));
  }

  public abstract MqttWritablePacket newSubscribeAck(
      int packetId,
      Array<SubscribeAckReasonCode> reasonCodes,
      String reason,
      MutableArray<StringPair> userProperties);

  public MqttWritablePacket newSubscribeAck(int packetId, Array<SubscribeAckReasonCode> reasonCodes) {
    return newSubscribeAck(packetId, reasonCodes, StringUtils.EMPTY, MutableArray.ofType(StringPair.class));
  }

  public abstract MqttWritablePacket newUnsubscribeAck(
      int packetId,
      Array<UnsubscribeAckReasonCode> reasonCodes,
      MutableArray<StringPair> userProperties,
      String reason);

  public MqttWritablePacket newUnsubscribeAck(int packetId, Array<UnsubscribeAckReasonCode> reasonCodes) {
    return newUnsubscribeAck(packetId, reasonCodes, MutableArray.ofType(StringPair.class), StringUtils.EMPTY);
  }

  public abstract MqttWritablePacket newDisconnect(
      MqttClient client,
      DisconnectReasonCode reasonCode,
      MutableArray<StringPair> userProperties,
      String reason,
      String serverReference);

  public MqttWritablePacket newDisconnect(MqttClient client, DisconnectReasonCode reasonCode) {
    return newDisconnect(
        client,
        reasonCode,
        MutableArray.ofType(StringPair.class),
        StringUtils.EMPTY,
        StringUtils.EMPTY);
  }

  public abstract MqttWritablePacket newAuthenticate(
      AuthenticateReasonCode reasonCode,
      String authenticateMethod,
      byte[] authenticateData,
      MutableArray<StringPair> userProperties,
      String reason);

  public MqttWritablePacket newAuthenticate(
      AuthenticateReasonCode reasonCode,
      String authenticateMethod,
      byte[] authenticateData) {
    return newAuthenticate(
        reasonCode,
        authenticateMethod,
        authenticateData,
        MutableArray.ofType(StringPair.class),
        StringUtils.EMPTY);
  }

  public abstract MqttWritablePacket newPingRequest();

  public abstract MqttWritablePacket newPingResponse();

  public abstract MqttWritablePacket newPublishRelease(
      int packetId,
      PublishReleaseReasonCode reasonCode,
      MutableArray<StringPair> userProperties,
      String reason);

  public MqttWritablePacket newPublishRelease(int packetId, PublishReleaseReasonCode reasonCode) {
    return newPublishRelease(packetId, reasonCode, MutableArray.ofType(StringPair.class), StringUtils.EMPTY);
  }

  public abstract MqttWritablePacket newPublishReceived(
      int packetId,
      PublishReceivedReasonCode reasonCode,
      MutableArray<StringPair> userProperties,
      String reason);

  public MqttWritablePacket newPublishReceived(int packetId, PublishReceivedReasonCode reasonCode) {
    return newPublishReceived(packetId, reasonCode, MutableArray.ofType(StringPair.class), StringUtils.EMPTY);
  }

  public abstract MqttWritablePacket newPublishCompleted(
      int packetId,
      PublishCompletedReasonCode reasonCode,
      MutableArray<StringPair> userProperties,
      String reason);

  public MqttWritablePacket newPublishCompleted(int packetId, PublishCompletedReasonCode reasonCode) {
    return newPublishCompleted(packetId, reasonCode, MutableArray.ofType(StringPair.class), StringUtils.EMPTY);
  }
}
