package com.ss.mqtt.broker.network.packet.in.handler;

import static reactor.core.publisher.Mono.fromRunnable;
import com.ss.mqtt.broker.exception.ConnectionRejectException;
import com.ss.mqtt.broker.exception.MalformedPacketMqttException;
import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import com.ss.mqtt.broker.network.client.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket;
import com.ss.mqtt.broker.service.AuthenticationService;
import com.ss.mqtt.broker.service.ClientIdRegistry;
import com.ss.rlib.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ConnectInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, ConnectInPacket> {

    private final ClientIdRegistry clientIdRegistry;
    private final AuthenticationService authenticationService;

    @Override
    protected void handleImpl(@NotNull UnsafeMqttClient client, @NotNull ConnectInPacket packet) {

        var connection = client.getConnection();
        connection.setMqttVersion(packet.getMqttVersion());

        if (checkPacketException(client, packet)) {
            return;
        }

        var requestedClientId = packet.getClientId();

        authenticationService.auth(packet.getUsername(), packet.getPassword())
            .filter(Boolean::booleanValue)
            .flatMap(auth -> registerClient(client, packet, requestedClientId))
            .switchIfEmpty(fromRunnable(() -> client.reject(ConnectAckReasonCode.BAD_USER_NAME_OR_PASSWORD)))
            .subscribe();
    }

    private Mono<?> registerClient(UnsafeMqttClient client, ConnectInPacket packet, String requestedClientId) {
        if (StringUtils.isEmpty(requestedClientId)) {
            return processWithoutClientId(client, packet, requestedClientId);
        } else {
            return processWithClientId(client, packet, requestedClientId);
        }
    }

    private Mono<?> processWithClientId(UnsafeMqttClient client, ConnectInPacket packet, String requestedClientId) {
        return clientIdRegistry.register(requestedClientId).doOnNext(result -> {
            if (!result) {
                client.reject(ConnectAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID);
            } else {
                client.setClientId(requestedClientId);
                onConnected(client, packet, requestedClientId);
            }
        });
    }

    private Mono<?> processWithoutClientId(UnsafeMqttClient client, ConnectInPacket packet, String requestedClientId) {
        return clientIdRegistry.generate()
            .doOnNext(client::setClientId)
            .flatMap(clientIdRegistry::register)
            .filter(Boolean::booleanValue)
            .then(fromRunnable(() -> onConnected(client, packet, requestedClientId)))
            .switchIfEmpty(fromRunnable(() -> client.reject(ConnectAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID)));
    }

    private void onConnected(
        @NotNull UnsafeMqttClient client,
        @NotNull ConnectInPacket packet,
        @NotNull String requestedClientId
    ) {

        client.configure(
            packet.getSessionExpiryInterval(),
            packet.getReceiveMax(),
            packet.getMaximumPacketSize(),
            packet.getTopicAliasMaximum(),
            packet.getKeepAlive()
        );

        client.send(client.getPacketOutFactory().newConnectAck(
            client,
            ConnectAckReasonCode.SUCCESS,
            false,
            requestedClientId,
            packet.getSessionExpiryInterval(),
            packet.getKeepAlive()
        ));
    }


    private boolean checkPacketException(@NotNull UnsafeMqttClient client, @NotNull ConnectInPacket packet) {

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
