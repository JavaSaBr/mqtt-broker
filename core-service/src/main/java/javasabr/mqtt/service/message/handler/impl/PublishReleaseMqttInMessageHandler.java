package javasabr.mqtt.service.message.handler.impl;

import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.publish.IncomingPublish;
import javasabr.mqtt.model.reason.code.PublishCompletedReasonCode;
import javasabr.mqtt.model.reason.code.PublishReleaseReasonCode;
import javasabr.mqtt.model.session.MessageTacker;
import javasabr.mqtt.model.session.ProcessingPublishes;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.in.PublishReleaseMqttInMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.service.MessageOutFactoryService;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishReleaseMqttInMessageHandler
    extends AbstractMqttInMessageHandler<ExternalNetworkMqttUser, PublishReleaseMqttInMessage> {

  public PublishReleaseMqttInMessageHandler(MessageOutFactoryService messageOutFactoryService) {
    super(ExternalNetworkMqttUser.class, PublishReleaseMqttInMessage.class, messageOutFactoryService);
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.PUBLISH_RELEASE;
  }

  @Override
  protected void processValidMessage(
      MqttConnection connection,
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      PublishReleaseMqttInMessage releaseMessage) {

    int messageId = releaseMessage.messageId();
    MessageTacker messageTacker = session.inMessageTracker();
    ProcessingPublishes<IncomingPublish> processingPublishes = session.incomingProcessingPublishes();

    if (releaseMessage.reasonCode() == PublishReleaseReasonCode.PACKET_IDENTIFIER_NOT_FOUND) {
      log.warn(user.clientId(), messageId, "[%s] Client doesn't know about messageId:[%d]"::formatted);
      messageTacker.removeIfExist(messageId);
      processingPublishes.remove(releaseMessage);
      return;
    }

    if (!processingPublishes.apply(user, releaseMessage)) {
      handleUnknownMessageId(user, messageId);
    }
  }

  private void handleUnknownMessageId(ExternalNetworkMqttUser client, int messageId) {
    client.sendInBackground(messageOutFactoryService
        .resolveFactory(client)
        .newPublishCompleted(messageId, PublishCompletedReasonCode.PACKET_IDENTIFIER_NOT_FOUND));
  }
}
