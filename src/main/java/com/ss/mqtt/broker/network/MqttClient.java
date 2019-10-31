package com.ss.mqtt.broker.network;

import com.ss.mqtt.broker.exception.ConnectionRejectException;
import com.ss.mqtt.broker.model.ConnectAckReasonCode;
import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.network.packet.PacketType;
import com.ss.mqtt.broker.network.packet.factory.MqttPacketOutFactory;
import com.ss.mqtt.broker.network.packet.in.ConnectInPacket;
import com.ss.mqtt.broker.network.packet.in.MqttReadablePacket;
import com.ss.mqtt.broker.network.packet.in.SubscribeInPacket;
import com.ss.mqtt.broker.network.packet.in.UnsubscribeInPacket;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

@Log4j2
@Getter
@EqualsAndHashCode(of = "clientId")
public class MqttClient {

    private final @NotNull MqttConnection connection;

    private volatile @NotNull String clientId;

    private volatile long sessionExpiryInterval = MqttPropertyConstants.SESSION_EXPIRY_INTERVAL_DEFAULT;
    private volatile int receiveMax = MqttPropertyConstants.RECEIVE_MAXIMUM_DEFAULT;
    private volatile int maximumPacketSize = MqttPropertyConstants.MAXIMUM_PACKET_SIZE_DEFAULT;
    private volatile int topicAliasMaximum = MqttPropertyConstants.TOPIC_ALIAS_MAXIMUM_DEFAULT;
    private volatile int keepAlive = MqttPropertyConstants.SERVER_KEEP_ALIVE_MAX;

    public MqttClient(@NotNull MqttConnection connection) {
        this.connection = connection;
        this.clientId = "";
    }

    public void handle(@NotNull MqttReadablePacket packet) {
        log.info("Handle received packet: {}", packet);
        switch (PacketType.fromByte(packet.getPacketType())) {
            case CONNECT:
                onConnected((ConnectInPacket) packet);
                break;
            case SUBSCRIBE:
                onSubscribe((SubscribeInPacket) packet);
                break;
            case UNSUBSCRIBE:
                onUnsubscribe((UnsubscribeInPacket) packet);
                break;
        }
    }

    public void reject(@NotNull ConnectAckReasonCode returnCode) {
        connection.send(getPacketOutFactory().newConnectAck(this, returnCode));
    }

    private void onConnected(@NotNull ConnectInPacket connect) {
        connection.setMqttVersion(connect.getMqttVersion());

        var exception = connect.getException();

        if (exception instanceof ConnectionRejectException) {

            var reasonCode = ((ConnectionRejectException) exception).getReasonCode();

            connection
                .sendWithFeedback(getPacketOutFactory().newConnectAck(this, reasonCode))
                .thenAccept(sent -> connection.close());

            return;
        }

        sessionExpiryInterval = connect.getSessionExpiryInterval();
        receiveMax = connect.getReceiveMax();
        maximumPacketSize = connect.getMaximumPacketSize();
        clientId = connect.getClientId();
        topicAliasMaximum = connect.getTopicAliasMaximum();

        connection.send(getPacketOutFactory().newConnectAck(
            this,
            ConnectAckReasonCode.SUCCESS,
            false,
            clientId,
            sessionExpiryInterval,
            keepAlive
        ));
    }

    private void onSubscribe(@NotNull SubscribeInPacket subscribe) {

        var ackReasonCodes = connection.getSubscriptionService()
            .subscribe(connection.getClient(), subscribe.getTopicFilters());

        connection.send(getPacketOutFactory().newSubscribeAck(connection.getClient(),
            subscribe.getPacketId(),
            ackReasonCodes
        ));
    }

    private void onUnsubscribe(@NotNull UnsubscribeInPacket subscribe) {

        var ackReasonCodes = connection.getSubscriptionService()
            .unsubscribe(connection.getClient(), subscribe.getTopicFilters());

        connection.send(getPacketOutFactory().newUnsubscribeAck(connection.getClient(),
            subscribe.getPacketId(),
            ackReasonCodes
        ));
    }

    private @NotNull MqttPacketOutFactory getPacketOutFactory() {
        return connection.getMqttVersion().getPacketOutFactory();
    }
}
