package javasabr.mqtt.service.handler.in;

import static javasabr.mqtt.base.utils.ReactorUtils.ifTrue;
import static javasabr.mqtt.model.MqttProperties.MAXIMUM_PACKET_SIZE_UNDEFINED;
import static javasabr.mqtt.model.MqttProperties.RECEIVE_MAXIMUM_UNDEFINED;
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
import javasabr.mqtt.model.exception.MalformedPacketMqttException;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.network.MqttConnection;
import javasabr.mqtt.network.MqttSession;
import javasabr.mqtt.network.packet.in.ConnectInPacket;
import javasabr.mqtt.service.AuthenticationService;
import javasabr.mqtt.service.ClientIdRegistry;
import javasabr.mqtt.service.MqttSessionService;
import javasabr.mqtt.service.SubscriptionService;
import javasabr.rlib.common.util.StringUtils;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@CustomLog
@RequiredArgsConstructor
public class ConnectInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, ConnectInPacket> {

  private final ClientIdRegistry clientIdRegistry;
  private final AuthenticationService authenticationService;
  private final MqttSessionService mqttSessionService;
  private final SubscriptionService subscriptionService;

  @Override
  protected void handleImpl(UnsafeMqttClient client, ConnectInPacket packet) {
    if (checkPacketException(client, packet)) {
      return;
    }
    resolveClientConnectionConfig(client, packet);
    authenticationService
        .auth(packet.username(), packet.password())
        .flatMap(ifTrue(client, packet, this::registerClient, BAD_USER_NAME_OR_PASSWORD, client::reject))
        .flatMap(ifTrue(client, packet, this::restoreSession, CLIENT_IDENTIFIER_NOT_VALID, client::reject))
        .subscribe();
  }

  private Mono<Boolean> registerClient(UnsafeMqttClient client, ConnectInPacket packet) {

    String requestedClientId = packet.clientId();
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

  private Mono<Boolean> restoreSession(UnsafeMqttClient client, ConnectInPacket packet) {

    if (packet.cleanStart()) {
      return mqttSessionService
          .create(client.clientId())
          .flatMap(session -> onConnected(client, packet, session, false));
    } else {
      return mqttSessionService
          .restore(client.clientId())
          .flatMap(session -> onConnected(client, packet, session, true))
          .switchIfEmpty(Mono.defer(() -> mqttSessionService
              .create(client.clientId())
              .flatMap(session -> onConnected(client, packet, session, false))));
    }
  }

  private void resolveClientConnectionConfig(UnsafeMqttClient client, ConnectInPacket packet) {

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
    int receiveMaxPublishes = packet.receiveMaxPublishes() == RECEIVE_MAXIMUM_UNDEFINED
                     ? serverConfig.receiveMaxPublishes()
                     : Math.min(packet.receiveMaxPublishes(), serverConfig.receiveMaxPublishes());

    // select result maximum packet size
    var maximumPacketSize = packet.maxPacketSize() == MAXIMUM_PACKET_SIZE_UNDEFINED
                            ? serverConfig.maxPacketSize()
                            : Math.min(packet.maxPacketSize(), serverConfig.maxPacketSize());

    // select result topic alias maximum
    var topicAliasMaxValue = packet.topicAliasMaxValue() == TOPIC_ALIAS_MAXIMUM_UNDEFINED
                            ? TOPIC_ALIAS_MAXIMUM_DISABLED
                            : Math.min(packet.topicAliasMaxValue(), serverConfig.topicAliasMaxValue());

    connection.configure(new MqttClientConnectionConfig(
        serverConfig.maxQos(),
        packet.mqttVersion(),
        sessionExpiryInterval,
        receiveMaxPublishes,
        maximumPacketSize,
        topicAliasMaxValue,
        keepAlive,
        packet.requestResponseInformation(),
        packet.requestProblemInformation(),
        serverConfig.sessionsEnabled(),
        serverConfig.retainAvailable(),
        serverConfig.wildcardSubscriptionAvailable(),
        serverConfig.subscriptionIdAvailable(),
        serverConfig.sharedSubscriptionAvailable()));
  }

  private Mono<Boolean> onConnected(
      UnsafeMqttClient client,
      ConnectInPacket packet,
      MqttSession session,
      boolean sessionRestored) {

    MqttConnection connection = client.connection();
    MqttServerConnectionConfig serverConfig = connection.serverConnectionConfig();
    MqttClientConnectionConfig clientConfig = connection.clientConnectionConfig();

    // if it was closed in parallel
    if (connection.closed() && serverConfig.sessionsEnabled()) {
      // store the session again
      return mqttSessionService.store(client.clientId(), session, clientConfig.sessionExpiryInterval());
    }

    client.session(session);

    var connectAck = client
        .packetOutFactory()
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

  private boolean onSentConnAck(UnsafeMqttClient client, MqttSession session, boolean result) {

    if (!result) {
      log.warning(client.clientId(), "Was issue with sending conn ack packet to client:[%s]"::formatted);
      return false;
    }

    session.resendPendingPackets(client);
    return true;
  }

  private boolean checkPacketException(UnsafeMqttClient client, ConnectInPacket packet) {
    Exception exception = packet.exception();
    if (exception instanceof ConnectionRejectException cre) {
      client.reject(cre.getReasonCode());
      return true;
    } else if (exception instanceof MalformedPacketMqttException) {
      client.reject(ConnectAckReasonCode.MALFORMED_PACKET);
      return true;
    }
    return false;
  }
}
