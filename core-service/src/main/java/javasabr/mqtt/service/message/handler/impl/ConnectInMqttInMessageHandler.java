package javasabr.mqtt.service.message.handler.impl;

import static javasabr.mqtt.base.util.ReactorUtils.ifTrue;
import static javasabr.mqtt.model.MqttProperties.MAXIMUM_MESSAGE_SIZE_IS_NOT_SET;
import static javasabr.mqtt.model.MqttProperties.RECEIVE_MAXIMUM_PUBLISHES_IS_NOT_SET;
import static javasabr.mqtt.model.MqttProperties.SERVER_KEEP_ALIVE_DISABLED;
import static javasabr.mqtt.model.MqttProperties.SESSION_EXPIRY_INTERVAL_DISABLED;
import static javasabr.mqtt.model.MqttProperties.SESSION_EXPIRY_INTERVAL_IS_NOT_SET;
import static javasabr.mqtt.model.MqttProperties.TOPIC_ALIAS_MAXIMUM_DISABLED;
import static javasabr.mqtt.model.MqttProperties.TOPIC_ALIAS_MAXIMUM_IS_NOT_SET;
import static javasabr.mqtt.model.reason.code.ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD;
import static javasabr.mqtt.model.reason.code.ConnectAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID;

import javasabr.mqtt.auth.api.AuthenticationMethod;
import javasabr.mqtt.auth.api.MqttCredentials;
import javasabr.mqtt.auth.api.AuthenticationService;
import javasabr.mqtt.model.MqttClientConnectionConfig;
import javasabr.mqtt.model.MqttServerConnectionConfig;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.exception.ConnectionRejectException;
import javasabr.mqtt.model.message.MqttMessageType;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.impl.ExternalNetworkMqttUser;
import javasabr.mqtt.network.message.in.ConnectMqttInMessage;
import javasabr.mqtt.network.message.out.MqttOutMessage;
import javasabr.mqtt.network.session.NetworkMqttSession;
import javasabr.mqtt.network.user.ConfigurableNetworkMqttUser;
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
    extends AbstractMqttInMessageHandler<ExternalNetworkMqttUser, ConnectMqttInMessage> {

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
    super(ExternalNetworkMqttUser.class, ConnectMqttInMessage.class, messageOutFactoryService);
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
  protected boolean requireSession() {
    return false;
  }

  @Override
  protected void processValidMessage(
      MqttConnection connection,
      ExternalNetworkMqttUser user,
      ConnectMqttInMessage message) {
    resolveClientConnectionConfig(user, message);
    MqttCredentials mqttCredentials = new MqttCredentials(
        message.username(),
        message.password(),
        AuthenticationMethod.fromValue(message.authenticationMethod()),
        message.authenticationData());
    authenticationService
        .authenticate(mqttCredentials)
        .flatMap(ifTrue(
            user,
            message, this::registerClient, BAD_USER_NAME_OR_PASSWORD, connectAckReasonCode -> reject(user, connectAckReasonCode)))
        .flatMap(ifTrue(
            user,
            message, this::restoreSession, CLIENT_IDENTIFIER_NOT_VALID, connectAckReasonCode -> reject(user, connectAckReasonCode)))
        .subscribe();
  }

  private void reject(ExternalNetworkMqttUser user, ConnectAckReasonCode connectAckReasonCode) {
    user.sendInBackground(messageOutFactoryService
        .resolveFactory(user)
        .newConnectAck(user, connectAckReasonCode));
  }

  private Mono<Boolean> registerClient(ExternalNetworkMqttUser user, ConnectMqttInMessage networkPacket) {

    String requestedClientId = networkPacket.clientId();
    if (StringUtils.isNotEmpty(requestedClientId)) {
      return clientIdRegistry
          .register(requestedClientId)
          .map(ifTrue(requestedClientId, user::clientId));
    }

    MqttVersion mqttVersion = user
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
            .map(ifTrue(newClientId, user::clientId)));
  }

  private Mono<Boolean> restoreSession(ConfigurableNetworkMqttUser user, ConnectMqttInMessage packet) {
    if (packet.cleanStart()) {
      return sessionService
          .create(user.clientId())
          .flatMap(session -> onConnected(user, packet, session, false));
    } else {
      return sessionService
          .restore(user.clientId())
          .flatMap(session -> onConnected(user, packet, session, true))
          .switchIfEmpty(Mono.defer(() -> sessionService
              .create(user.clientId())
              .flatMap(session -> onConnected(user, packet, session, false))));
    }
  }

  private void resolveClientConnectionConfig(ConfigurableNetworkMqttUser user, ConnectMqttInMessage packet) {

    MqttConnection connection = user.connection();
    MqttServerConnectionConfig serverConfig = connection.serverConnectionConfig();

    // select result keep alive time
    int minimalKeepAliveTime = Math.max(serverConfig.minKeepAliveTime(), packet.keepAlive());
    int keepAlive = serverConfig.keepAliveEnabled() ? minimalKeepAliveTime : SERVER_KEEP_ALIVE_DISABLED;

    // select result session expiry interval
    long sessionExpiryInterval = serverConfig.sessionsEnabled()
                                 ? packet.sessionExpiryInterval()
                                 : SESSION_EXPIRY_INTERVAL_DISABLED;

    if (sessionExpiryInterval == SESSION_EXPIRY_INTERVAL_IS_NOT_SET) {
      sessionExpiryInterval = serverConfig.defaultSessionExpiryInterval();
    }

    // select result receive max
    int receiveMaxPublishes = packet.receiveMaxPublishes() == RECEIVE_MAXIMUM_PUBLISHES_IS_NOT_SET
                              ? serverConfig.receiveMaxPublishes()
                              : Math.min(packet.receiveMaxPublishes(), serverConfig.receiveMaxPublishes());

    // select result maximum packet size
    var maximumPacketSize = packet.maxPacketSize() == MAXIMUM_MESSAGE_SIZE_IS_NOT_SET
                            ? serverConfig.maxMessageSize()
                            : Math.min(packet.maxPacketSize(), serverConfig.maxMessageSize());

    // select result topic alias maximum
    var topicAliasMaxValue = packet.topicAliasMaxValue() == TOPIC_ALIAS_MAXIMUM_IS_NOT_SET
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
      ConfigurableNetworkMqttUser user,
      ConnectMqttInMessage message,
      NetworkMqttSession session,
      boolean sessionRestored) {

    MqttConnection connection = user.connection();
    MqttServerConnectionConfig serverConfig = connection.serverConnectionConfig();
    MqttClientConnectionConfig clientConfig = connection.clientConnectionConfig();

    // if it was closed in parallel
    if (connection.closed() && serverConfig.sessionsEnabled()) {
      // store the session again
      return sessionService.store(user.clientId(), session, clientConfig.sessionExpiryInterval());
    }

    user.session(session);

    var connectAck = messageOutFactoryService
        .resolveFactory(user)
        .newConnectAck(
            user,
            ConnectAckReasonCode.SUCCESS,
            sessionRestored,
            message.clientId(),
            message.sessionExpiryInterval(),
            message.keepAlive(),
            message.receiveMaxPublishes());

    subscriptionService.restoreSubscriptions(user, session);

    return Mono.fromFuture(user
        .sendAsync(connectAck)
        .thenApply(result -> onSentConnAck(user, session, result)));
  }

  private boolean onSentConnAck(ConfigurableNetworkMqttUser user, NetworkMqttSession session, boolean result) {

    if (!result) {
      log.warning(user.clientId(), "Was issue with sending conn ack packet to client:[%s]"::formatted);
      return false;
    }

    session.resendNotConfirmedPublishesTo(user);
    return true;
  }

  @Override
  protected boolean processInvalidMessage(
      MqttConnection connection,
      ExternalNetworkMqttUser user,
      NetworkMqttSession session,
      ConnectMqttInMessage message) {
    Exception exception = message.exception();
    if (exception instanceof ConnectionRejectException cre) {
      MqttOutMessage feedback = messageOutFactoryService
          .resolveFactory(user)
          .newConnectAck(user, cre.reasonCode());
      user.closeWithReason(feedback);
      return true;
    }
    return super.processInvalidMessage(connection, user, session, message);
  }
}
