package javasabr.mqtt.service.message.out.factory;

import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.QoS;
import javasabr.mqtt.model.data.type.StringPair;
import javasabr.mqtt.model.publish.PublishData;
import javasabr.mqtt.model.reason.code.AuthenticateReasonCode;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.model.reason.code.DisconnectReasonCode;
import javasabr.mqtt.model.reason.code.PublishAckReasonCode;
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode;
import javasabr.mqtt.model.reason.code.PublishReceivedReasonCode;
import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode;
import javasabr.mqtt.model.reason.code.SubscribeAckReasonCode;
import javasabr.mqtt.model.reason.code.UnsubscribeAckReasonCode;
import javasabr.mqtt.model.topic.TopicName;
import javasabr.mqtt.network.message.out.ConnectAckMqtt311OutMessage;
import javasabr.mqtt.network.message.out.DisconnectMqtt311OutMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.message.out.PingRequestMqtt311OutMessage;
import javasabr.mqtt.network.message.out.PingResponseMqtt311OutMessage;
import javasabr.mqtt.network.message.out.PublishAckMqtt311OutMessage;
import javasabr.mqtt.network.message.out.PublishCompleteMqtt311OutMessage;
import javasabr.mqtt.network.message.out.PublishMqtt311OutMessage;
import javasabr.mqtt.network.message.out.PublishReceivedMqtt311OutMessage;
import javasabr.mqtt.network.message.out.PublishReleaseMqtt311OutMessage;
import javasabr.mqtt.network.message.out.SubscribeAckMqtt311OutMessage;
import javasabr.mqtt.network.message.out.UnsubscribeAckMqtt311OutMessage;
import javasabr.mqtt.network.user.NetworkMqttUser;
import javasabr.rlib.collections.array.Array;
import org.jspecify.annotations.Nullable;

public class Mqtt311MessageOutFactory extends MqttMessageOutFactory {

  @Override
  public MqttVersion mqttVersion() {
    return MqttVersion.MQTT_3_1_1;
  }

  @Override
  public MqttOutMessage newConnectAck(
      NetworkMqttUser user,
      ConnectAckReasonCode reasonCode,
      boolean sessionPresent,
      String requestedClientId,
      long requestedSessionExpiryInterval,
      int requestedKeepAlive,
      int requestedReceiveMaxPublishes,
      String reason,
      String serverReference,
      String responseInformation,
      String authenticationMethod,
      byte[] authenticationData,
      Array<StringPair> userProperties) {
    return new ConnectAckMqtt311OutMessage(reasonCode, sessionPresent);
  }

  @Override
  public MqttOutMessage newPublish(
      int messageId,
      QoS qos,
      boolean retain,
      boolean duplicate,
      TopicName topicName,
      int topicAlias,
      PublishData data,
      @Nullable TopicName responseTopic,
      Array<StringPair> userProperties) {
    return new PublishMqtt311OutMessage(messageId, qos, retain, duplicate, topicName, data);
  }

  @Override
  public MqttOutMessage newPublishAck(
      int messageId,
      PublishAckReasonCode reasonCode,
      @Nullable String reason,
      Array<StringPair> userProperties) {
    return new PublishAckMqtt311OutMessage(messageId);
  }

  @Override
  public MqttOutMessage newSubscribeAck(
      int messageId,
      Array<SubscribeAckReasonCode> reasonCodes,
      String reason,
      Array<StringPair> userProperties) {
    return new SubscribeAckMqtt311OutMessage(messageId, reasonCodes);
  }

  @Override
  public MqttOutMessage newUnsubscribeAck(
      int messageId,
      Array<UnsubscribeAckReasonCode> reasonCodes,
      Array<StringPair> userProperties,
      String reason) {
    return new UnsubscribeAckMqtt311OutMessage(messageId);
  }

  @Override
  public MqttOutMessage newDisconnect(
      NetworkMqttUser user,
      DisconnectReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason,
      String serverReference) {
    return new DisconnectMqtt311OutMessage();
  }

  @Override
  public MqttOutMessage newAuthenticate(
      AuthenticateReasonCode reasonCode,
      @Nullable String reason,
      @Nullable String authenticateMethod,
      byte @Nullable [] authenticateData,
      Array<StringPair> userProperties) {
    throw new UnsupportedOperationException();
  }

  @Override
  public MqttOutMessage newPingRequest() {
    return new PingRequestMqtt311OutMessage();
  }

  @Override
  public MqttOutMessage newPingResponse() {
    return new PingResponseMqtt311OutMessage();
  }

  @Override
  public MqttOutMessage newPublishRelease(
      int messageId,
      PublishReleaseReasonCode reasonCode,
      Array<StringPair> userProperties,
      String reason) {
    return new PublishReleaseMqtt311OutMessage(messageId);
  }

  @Override
  public MqttOutMessage newPublishReceived(
      int messageId,
      PublishReceivedReasonCode reasonCode,
      Array<StringPair> userProperties,
      @Nullable String reason) {
    return new PublishReceivedMqtt311OutMessage(messageId);
  }

  @Override
  public MqttOutMessage newPublishCompleted(
      int messageId,
      PublishCompletedReasonCode reasonCode,
      Array<StringPair> userProperties,
      @Nullable String reason) {
    return new PublishCompleteMqtt311OutMessage(messageId);
  }
}
