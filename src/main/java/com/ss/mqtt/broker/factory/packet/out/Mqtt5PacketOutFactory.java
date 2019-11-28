package com.ss.mqtt.broker.factory.packet.out;

import com.ss.mqtt.broker.model.*;
import com.ss.mqtt.broker.model.data.type.StringPair;
import com.ss.mqtt.broker.model.reason.code.*;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.out.*;
import com.ss.rlib.common.util.array.Array;
import org.jetbrains.annotations.NotNull;

public class Mqtt5PacketOutFactory extends Mqtt311PacketOutFactory {

    @Override
    public @NotNull MqttWritablePacket newConnectAck(
        @NotNull MqttClient client,
        @NotNull ConnectAckReasonCode reasonCode,
        boolean sessionPresent,
        @NotNull String requestedClientId,
        long requestedSessionExpiryInterval,
        int requestedKeepAlive,
        int requestedReceiveMax,
        @NotNull String reason,
        @NotNull String serverReference,
        @NotNull String responseInformation,
        @NotNull String authenticationMethod,
        @NotNull byte[] authenticationData,
        @NotNull Array<StringPair> userProperties
    ) {
        var config = client.getConnectionConfig();
        return new ConnectAck5OutPacket(
            reasonCode,
            sessionPresent,
            requestedClientId,
            requestedSessionExpiryInterval,
            requestedKeepAlive,
            requestedReceiveMax,
            reason,
            serverReference,
            responseInformation,
            authenticationMethod,
            authenticationData,
            userProperties,
            client.getClientId(),
            config.getMaxQos(),
            client.getSessionExpiryInterval(),
            client.getMaximumPacketSize(),
            client.getReceiveMax(),
            client.getTopicAliasMaximum(),
            client.getKeepAlive(),
            config.isRetainAvailable(),
            config.isWildcardSubscriptionAvailable(),
            config.isSubscriptionIdAvailable(),
            config.isSharedSubscriptionAvailable()
        );
    }

    @Override
    public @NotNull PublishOutPacket newPublish(
        int packetId,
        @NotNull QoS qos,
        boolean retained,
        boolean duplicate,
        @NotNull String topicName,
        int topicAlias,
        @NotNull byte[] payload,
        boolean stringPayload,
        @NotNull String responseTopic,
        @NotNull byte[] correlationData,
        @NotNull Array<StringPair> userProperties
    ) {
        return new Publish5OutPacket(
            packetId,
            qos,
            retained,
            duplicate,
            topicName,
            topicAlias,
            payload,
            stringPayload,
            responseTopic,
            correlationData,
            userProperties
        );
    }

    @Override
    public @NotNull MqttWritablePacket newPublishAck(
        int packetId,
        @NotNull PublishAckReasonCode reasonCode,
        @NotNull String reason,
        @NotNull Array<StringPair> userProperties
    ) {
        return new PublishAck5OutPacket(packetId, reasonCode, userProperties, reason);
    }

    @Override
    public @NotNull MqttWritablePacket newSubscribeAck(
        int packetId,
        @NotNull Array<SubscribeAckReasonCode> reasonCodes,
        @NotNull String reason,
        @NotNull Array<StringPair> userProperties
    ) {
        return new SubscribeAck5OutPacket(packetId, reasonCodes, userProperties, reason);
    }

    @Override
    public @NotNull MqttWritablePacket newUnsubscribeAck(
        int packetId,
        @NotNull Array<UnsubscribeAckReasonCode> reasonCodes,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        return new UnsubscribeAck5OutPacket(packetId, reasonCodes, userProperties, reason);
    }

    @Override
    public @NotNull MqttWritablePacket newDisconnect(
        @NotNull MqttClient client,
        @NotNull DisconnectReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason,
        @NotNull String serverReference
    ) {
        return new Disconnect5OutPacket(
            reasonCode,
            userProperties,
            reason,
            serverReference,
            client.getSessionExpiryInterval()
        );
    }

    @Override
    public @NotNull MqttWritablePacket newAuthenticate(
        @NotNull AuthenticateReasonCode reasonCode,
        @NotNull String authenticateMethod,
        @NotNull byte[] authenticateData,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        return new Authentication5OutPacket(
            userProperties,
            reasonCode,
            reason,
            authenticateMethod,
            authenticateData
        );
    }

    @Override
    public @NotNull MqttWritablePacket newPublishRelease(
        int packetId,
        @NotNull PublishReleaseReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        return new PublishRelease5OutPacket(packetId, reasonCode, userProperties, reason);
    }

    @Override
    public @NotNull MqttWritablePacket newPublishReceived(
        int packetId,
        @NotNull PublishReceivedReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        return new PublishReceived5OutPacket(packetId, reasonCode, userProperties, reason);
    }

    @Override
    public @NotNull MqttWritablePacket newPublishCompleted(
        int packetId,
        @NotNull PublishCompletedReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        return new PublishComplete5OutPacket(packetId, reasonCode, userProperties, reason);
    }
}
