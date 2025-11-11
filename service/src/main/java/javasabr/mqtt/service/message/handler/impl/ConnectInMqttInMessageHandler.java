package javasabr.mqtt.service.message.handler.impl;

import static javasabr.mqtt.base.util.ReactorUtils.ifTrue;
import static javasabr.mqtt.model.MqttProperties.MAXIMUM_MESSAGE_SIZE_UNDEFINED;
import static javasabr.mqtt.model.MqttProperties.RECEIVE_MAXIMUM_PUBLISHES_UNDEFINED;
import static javasabr.mqtt.model.MqttProperties.SERVER_KEEP_ALIVE_DISABLED;
import static javasabr.mqtt.model.MqttProperties.SESSION_EXPIRY_INTERVAL_DISABLED;
import static javasabr.mqtt.model.MqttProperties.SESSION_EXPIRY_INTERVAL_UNDEFINED;
import static javasabr.mqtt.model.MqttProperties.TOPIC_ALIAS_MAXIMUM_DISABLED;
import static javasabr.mqtt.model.MqttProperties.TOPIC_ALIAS_MAXIMUM_UNDEFINED;
import static javasabr.mqtt.model.reason.code.ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD;
import static javasabr.mqtt.model.reason.code.ConnectAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID;

import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.exception.ConnectionRejectException;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.network.MqttClient;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalMqttClient;
import javasabr.mqtt.network.message.MqttMessageType;
import javasabr.mqtt.network.message.in.ConnectMqttInMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.MqttSession;
import javasabr.mqtt.service.AuthenticationService;
import javasabr.mqtt.service.ClientIdRegistry;
import javasabr.mqtt.service.MessageOutFactoryService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.mqtt.service.session.MqttSessionService;
import javasabr.rlib.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Mono;

@CustomLog
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConnectInMqttInMessageHandler
    extends AbstractMqttInMessageHandler<ExternalMqttClient, ConnectMqttInMessage> {

  ClientIdRegistry clientIdRegistry;
  AuthenticationService authenticationService;
  MqttSessionService sessionService;
  SubscriptionService subscriptionService;

  public ConnectInMqttInMessageHandler(
      ClientIdRegistry clientIdRegistry,
      AuthenticationService authenticationService,
      MqttSessionService sessionService,
      SubscriptionService subscriptionService,
      MessageOutFactoryService messageOutFactoryService) {
    super(ExternalMqttClient.class, ConnectMqttInMessage.class, messageOutFactoryService);
    this.clientIdRegistry = clientIdRegistry;
    this.authenticationService = authenticationService;
    this.sessionService = sessionService;
    this.subscriptionService = subscriptionService;
  }

  @Override
  public MqttMessageType messageType() {
    return MqttMessageType.CONNECT;
  }

  @Override
  protected void processValidMessage(
      MqttConnection connection,
      ExternalMqttClient client,
      MqttSession session,
      ConnectMqttInMessage message) {
    resolveClientConnectionConfig(client, message);
    authenticationService
        .auth(message.username(), message.password())
        .flatMap(ifTrue(client,
            message, this::registerClient, BAD_USER_NAME_OR_PASSWORD, connectAckReasonCode -> reject(client, connectAckReasonCode)))
        .flatMap(ifTrue(client,
            message, this::restoreSession, CLIENT_IDENTIFIER_NOT_VALID, connectAckReasonCode -> reject(client, connectAckReasonCode)))
        .subscribe();
  }

  private void reject(ExternalMqttClient client, ConnectAckReasonCode connectAckReasonCode) {
    client.send(messageOutFactoryService
        .resolveFactory(client)
        .newConnectAck(client, connectAckReasonCode));
  }

  private Mono<Boolean> registerClient(ExternalMqttClient client, ConnectMqttInMessage networkPacket) {

    String requestedClientId = networkPacket.clientId();
    if (StringUtils.isNotEmpty(requestedClientId)) {
      return clientIdRegistry
          .register(requestedClientId)
          .map(ifTrue(requestedClientId, client::clientId));
    }

    MqttVersion mqttVersion = client
        .connection()
        .clientConnectionConfig()
        .mqttVersion();

    // we can't assign generated client id for mqtt version less than 5
    if (mqttVersion.isLowerThan(MqttVersion.MQTT_5)) {
      return Mono.just(false);
    }

    return clientIdRegistry
        .generate()
        .flatMap(newClientId -> clientIdRegistry
            .register(newClientId)
            .map(ifTrue(newClientId, client::clientId)));
  }

  private Mono<Boolean> restoreSession(MqttClient.UnsafeMqttClient client, ConnectMqttInMessage packet) {
    if (packet.cleanStart()) {
      return sessionService
          .create(client.clientId())
          .flatMap(session -> onConnected(client, packet, session, false));
    } else {
      return sessionService
          .restore(client.clientId())
          .flatMap(session -> onConnected(client, packet, session, true))
          .switchIfEmpty(Mono.defer(() -> sessionService
              .create(client.clientId())
              .flatMap(session -> onConnected(client, packet, session, false))));
    }
  }

  private void resolveClientConnectionConfig(MqttClient.UnsafeMqttClient client, ConnectMqttInMessage packet) {

    MqttConnection connection = client.connection();
    MqttServerConnectionConfig serverConfig = connection.serverConnectionConfig();

    // select result keep alive time
    int minimalKeepAliveTime = Math.max(serverConfig.minKeepAliveTime(), packet.keepAlive());
    int keepAlive = serverConfig.keepAliveEnabled() ? minimalKeepAliveTime : SERVER_KEEP_ALIVE_DISABLED;

    // select result session expiry interval
    long sessionExpiryInterval = serverConfig.sessionsEnabled()
                                 ? packet.sessionExpiryInterval()
                                 : SESSION_EXPIRY_INTERVAL_DISABLED;

    if (sessionExpiryInterval == SESSION_EXPIRY_INTERVAL_UNDEFINED) {
      sessionExpiryInterval = serverConfig.defaultSessionExpiryInterval();
    }

    // select result receive max
    int receiveMaxPublishes = packet.receiveMaxPublishes() == RECEIVE_MAXIMUM_PUBLISHES_UNDEFINED
                              ? serverConfig.receiveMaxPublishes()
                              : Math.min(packet.receiveMaxPublishes(), serverConfig.receiveMaxPublishes());

    // select result maximum packet size
    var maximumPacketSize = packet.maxPacketSize() == MAXIMUM_MESSAGE_SIZE_UNDEFINED
                            ? serverConfig.maxMessageSize()
                            : Math.min(packet.maxPacketSize(), serverConfig.maxMessageSize());

    // select result topic alias maximum
    var topicAliasMaxValue = packet.topicAliasMaxValue() == TOPIC_ALIAS_MAXIMUM_UNDEFINED
                             ? TOPIC_ALIAS_MAXIMUM_DISABLED
                             : Math.min(packet.topicAliasMaxValue(), serverConfig.topicAliasMaxValue());

    connection.configure(new MqttClientConnectionConfig(
        serverConfig,
        serverConfig.maxQos(),
        packet.mqttVersion(),
        sessionExpiryInterval,
        receiveMaxPublishes,
        maximumPacketSize,
        topicAliasMaxValue,
        keepAlive,
        packet.requestResponseInformation(),
        packet.requestProblemInformation()));
  }

  private Mono<Boolean> onConnected(
      MqttClient.UnsafeMqttClient client,
      ConnectMqttInMessage packet,
      MqttSession session,
      boolean sessionRestored) {

    MqttConnection connection = client.connection();
    MqttServerConnectionConfig serverConfig = connection.serverConnectionConfig();
    MqttClientConnectionConfig clientConfig = connection.clientConnectionConfig();

    // if it was closed in parallel
    if (connection.closed() && serverConfig.sessionsEnabled()) {
      // store the session again
      return sessionService.store(client.clientId(), session, clientConfig.sessionExpiryInterval());
    }

    client.session(session);

    var connectAck = messageOutFactoryService
        .resolveFactory(client)
        .newConnectAck(
            client,
            ConnectAckReasonCode.SUCCESS,
            sessionRestored,
            packet.clientId(),
            packet.sessionExpiryInterval(),
            packet.keepAlive(),
            packet.receiveMaxPublishes());

    subscriptionService.restoreSubscriptions(client, session);

    return Mono.fromFuture(client
        .sendWithFeedback(connectAck)
        .thenApply(result -> onSentConnAck(client, session, result)));
  }

  private boolean onSentConnAck(MqttClient.UnsafeMqttClient client, MqttSession session, boolean result) {

    if (!result) {
      log.warning(client.clientId(), "Was issue with sending conn ack packet to client:[%s]"::formatted);
      return false;
    }

    session.resendPendingPackets(client);
    return true;
  }

  @Override
  protected boolean processInvalidMessage(
      MqttConnection connection,
      ExternalMqttClient client,
      MqttSession session,
      ConnectMqttInMessage message) {
    Exception exception = message.exception();
    if (exception instanceof ConnectionRejectException cre) {
      MqttOutMessage feedback = messageOutFactoryService
          .resolveFactory(client)
          .newConnectAck(client, cre.getReasonCode());
      client.closeWithReason(feedback);
      return true;
    }
    return super.processInvalidMessage(connection, client, session, message);
  }
}
