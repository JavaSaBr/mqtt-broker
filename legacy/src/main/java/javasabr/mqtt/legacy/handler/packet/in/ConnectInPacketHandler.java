package javasabr.mqtt.legacy.handler.packet.in;

import static javasabr.mqtt.model.MqttProperties.MAXIMUM_PACKET_SIZE_UNDEFINED;
import static javasabr.mqtt.model.MqttProperties.RECEIVE_MAXIMUM_UNDEFINED;
import static javasabr.mqtt.model.MqttProperties.SERVER_KEEP_ALIVE_DISABLED;
import static javasabr.mqtt.model.MqttProperties.SESSION_EXPIRY_INTERVAL_DISABLED;
import static javasabr.mqtt.model.MqttProperties.SESSION_EXPIRY_INTERVAL_UNDEFINED;
import static javasabr.mqtt.model.MqttProperties.TOPIC_ALIAS_MAXIMUM_DISABLED;
import static javasabr.mqtt.model.MqttProperties.TOPIC_ALIAS_MAXIMUM_UNDEFINED;
import static javasabr.mqtt.model.reason.code.ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD;
import static javasabr.mqtt.model.reason.code.ConnectAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID;
import static javasabr.mqtt.legacy.util.ReactorUtils.ifTrue;

import javasabr.mqtt.legacy.exception.ConnectionRejectException;
import javasabr.mqtt.legacy.exception.MalformedPacketMqttException;
import javasabr.mqtt.legacy.network.MqttSession;
import javasabr.mqtt.model.MqttVersion;
import javasabr.mqtt.model.reason.code.ConnectAckReasonCode;
import javasabr.mqtt.legacy.network.MqttClient.UnsafeMqttClient;
import javasabr.mqtt.legacy.network.packet.in.ConnectInPacket;
import javasabr.mqtt.legacy.service.AuthenticationService;
import javasabr.mqtt.legacy.service.ClientIdRegistry;
import javasabr.mqtt.legacy.service.MqttSessionService;
import javasabr.mqtt.legacy.service.SubscriptionService;
import javasabr.rlib.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
@RequiredArgsConstructor
public class ConnectInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, ConnectInPacket> {

  private final ClientIdRegistry clientIdRegistry;
  private final AuthenticationService authenticationService;
  private final MqttSessionService mqttSessionService;
  private final SubscriptionService subscriptionService;

  @Override
  protected void handleImpl(UnsafeMqttClient client, ConnectInPacket packet) {

    var connection = client.getConnection();
    connection.setMqttVersion(packet.getMqttVersion());

    if (checkPacketException(client, packet)) {
      return;
    }

    authenticationService
        .auth(packet.getUsername(), packet.getPassword())
        .flatMap(ifTrue(client, packet, this::registerClient, BAD_USER_NAME_OR_PASSWORD, client::reject))
        .flatMap(ifTrue(client, packet, this::restoreSession, CLIENT_IDENTIFIER_NOT_VALID, client::reject))
        .subscribe();
  }

  private Mono<Boolean> registerClient(UnsafeMqttClient client, ConnectInPacket packet) {

    var requestedClientId = packet.getClientId();

    if (StringUtils.isNotEmpty(requestedClientId)) {
      return clientIdRegistry
          .register(requestedClientId)
          .map(ifTrue(requestedClientId, client::setClientId));
    } else {

      var mqttVersion = client
          .getConnection()
          .getMqttVersion();

      // we can't assign generated client if for mqtt version less than 5
      if (mqttVersion.ordinal() < MqttVersion.MQTT_5.ordinal()) {
        return Mono.just(false);
      }

      return clientIdRegistry
          .generate()
          .flatMap(newClientId -> clientIdRegistry
              .register(newClientId)
              .map(ifTrue(newClientId, client::setClientId)));
    }
  }

  private Mono<Boolean> restoreSession(UnsafeMqttClient client, ConnectInPacket packet) {

    if (packet.isCleanStart()) {
      return mqttSessionService
          .create(client.getClientId())
          .flatMap(session -> onConnected(client, packet, session, false));
    } else {
      return mqttSessionService
          .restore(client.getClientId())
          .flatMap(session -> onConnected(client, packet, session, true))
          .switchIfEmpty(Mono.defer(() -> mqttSessionService
              .create(client.getClientId())
              .flatMap(session -> onConnected(client, packet, session, false))));
    }
  }

  private Mono<Boolean> onConnected(
      UnsafeMqttClient client,
      ConnectInPacket packet,
      MqttSession session,
      boolean sessionRestored) {

    var connection = client.getConnection();
    var config = connection.getConfig();

    // if it was closed in parallel
    if (connection.isClosed() && config.isSessionsEnabled()) {
      // store the session again
      return mqttSessionService.store(client.getClientId(), session, config.getDefaultSessionExpiryInterval());
    }

    // select result keep alive time
    var minimalKeepAliveTime = Math.max(config.getMinKeepAliveTime(), packet.getKeepAlive());
    var keepAlive = config.isKeepAliveEnabled() ? minimalKeepAliveTime : SERVER_KEEP_ALIVE_DISABLED;

    // select result session expiry interval
    var sessionExpiryInterval = config.isSessionsEnabled()
                                ? packet.getSessionExpiryInterval()
                                : SESSION_EXPIRY_INTERVAL_DISABLED;

    if (sessionExpiryInterval == SESSION_EXPIRY_INTERVAL_UNDEFINED) {
      sessionExpiryInterval = config.getDefaultSessionExpiryInterval();
    }

    // select result receive max
    var receiveMax = packet.getReceiveMax() == RECEIVE_MAXIMUM_UNDEFINED
                     ? config.getReceiveMaximum()
                     : Math.min(packet.getReceiveMax(), config.getReceiveMaximum());

    // select result maximum packet size
    var maximumPacketSize = packet.getMaximumPacketSize() == MAXIMUM_PACKET_SIZE_UNDEFINED
                            ? config.getMaximumPacketSize()
                            : Math.min(packet.getMaximumPacketSize(), config.getMaximumPacketSize());

    // select result topic alias maximum
    var topicAliasMaximum = packet.getTopicAliasMaximum() == TOPIC_ALIAS_MAXIMUM_UNDEFINED
                            ? TOPIC_ALIAS_MAXIMUM_DISABLED
                            : Math.min(packet.getTopicAliasMaximum(), config.getTopicAliasMaximum());

    client.setSession(session);
    client.configure(
        sessionExpiryInterval,
        receiveMax,
        maximumPacketSize,
        topicAliasMaximum,
        keepAlive,
        packet.isRequestResponseInformation(),
        packet.isRequestProblemInformation());

    var connectAck = client
        .getPacketOutFactory()
        .newConnectAck(
            client,
            ConnectAckReasonCode.SUCCESS,
            sessionRestored,
            packet.getClientId(),
            packet.getSessionExpiryInterval(),
            packet.getKeepAlive(),
            packet.getReceiveMax());

    subscriptionService.restoreSubscriptions(client, session);

    return Mono.fromFuture(client
        .sendWithFeedback(connectAck)
        .thenApply(result -> onSentConnAck(client, session, result)));
  }

  private boolean onSentConnAck(UnsafeMqttClient client, MqttSession session, boolean result) {

    if (!result) {
      log.warn("Was issue with sending conn ack packet to client {}", client.getClientId());
      return false;
    }

    session.resendPendingPackets(client);
    return true;
  }

  private boolean checkPacketException(UnsafeMqttClient client, ConnectInPacket packet) {

    var exception = packet.getException();

    if (exception instanceof ConnectionRejectException) {
      client.reject(((ConnectionRejectException) exception).getReasonCode());
      return true;
    } else if (exception instanceof MalformedPacketMqttException) {
      client.reject(ConnectAckReasonCode.MALFORMED_PACKET);
      return true;
    }

    return false;
  }
}
