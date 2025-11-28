package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode;
import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.ProcessingPublishes;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.in.PublishReleaseMqttInMessage;
import javasabr.mqtt.network.session.MqttNetworkSession;
import javasabr.mqtt.service.MessageOutFactoryService;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishReleaseMqttInMessageHandler
    extends AbstractMqttInMessageHandler<ExternalMqttClient, PublishReleaseMqttInMessage> {

  public PublishReleaseMqttInMessageHandler(MessageOutFactoryService messageOutFactoryService) {
    super(ExternalMqttClient.class, PublishReleaseMqttInMessage.class, messageOutFactoryService);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_RELEASE;
  }

  @Override
  protected void processValidMessage(
      MqttConnection connection,
      ExternalMqttClient client,
      MqttNetworkSession session,
      PublishReleaseMqttInMessage releaseMessage) {

    int messageId = releaseMessage.messageId();
    MessageTacker messageTacker = session.inMessageTracker();
    ProcessingPublishes processingPublishes = session.inProcessingPublishes();

    if (releaseMessage.reasonCode() == PublishReleaseReasonCode.PACKET_IDENTIFIER_NOT_FOUND) {
      log.warning(client.clientId(), messageId, "[%s] Client doesnt know about messageId:[%d]"::formatted);
      messageTacker.remove(messageId);
      processingPublishes.remove(releaseMessage);
      return;
    }

    if (!processingPublishes.apply(client, releaseMessage)) {
      handleUnknownMessageId(client, messageId);
    }
  }

  private void handleUnknownMessageId(ExternalMqttClient client, int messageId) {
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newPublishCompleted(messageId, PublishCompletedReasonCode.PACKET_IDENTIFIER_NOT_FOUND));
  }
}
