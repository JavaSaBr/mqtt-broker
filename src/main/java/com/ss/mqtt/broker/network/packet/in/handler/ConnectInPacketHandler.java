package com.ss.mqtt.broker.network.packet.in.handler;

import static reactor.core.publisher.Mono.fromRunnable;
import com.ss.mqtt.broker.exception.ConnectionRejectException;
import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import com.ss.mqtt.broker.network.client.UnsafeMqttClient;
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket;
import com.ss.mqtt.broker.service.ClientIdRegistry;
import com.ss.rlib.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ConnectInPacketHandler extends AbstractPacketHandler<UnsafeMqttClient, ConnectInPacket> {

    private final ClientIdRegistry clientIdRegistry;

    @Override
    protected void handleImpl(@NotNull UnsafeMqttClient client, @NotNull ConnectInPacket packet) {

        var connection = client.getConnection();
        connection.setMqttVersion(packet.getMqttVersion());

        if (checkPacketException(client, packet)) {
            return;
        }

        var requestedClientId = packet.getClientId();

        // if we should assign our client id
        if (StringUtils.isEmpty(requestedClientId)) {
            clientIdRegistry.generate()
                .doOnNext(client::setClientId)
                .flatMap(clientIdRegistry::register)
                .filter(Boolean::booleanValue)
                .then(fromRunnable(() -> onConnected(client, packet, requestedClientId)))
                .switchIfEmpty(fromRunnable(() -> client.reject(ConnectAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID)))
                .subscribe();
        } else {

            if (!clientIdRegistry.validate(requestedClientId)) {
                client.reject(ConnectAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID);
                return;
            }

            clientIdRegistry.register(requestedClientId)
                .subscribe(result -> {
                    if(!result) {
                        client.reject(ConnectAckReasonCode.CLIENT_IDENTIFIER_NOT_VALID);
                    } else {
                        client.setClientId(requestedClientId);
                        onConnected(client, packet, requestedClientId);
                    }
                });
        }
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
        }

        return false;
    }
}
