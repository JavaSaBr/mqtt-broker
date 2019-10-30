package com.ss.mqtt.broker.network.packet.factory;

import com.ss.mqtt.broker.model.*;
import com.ss.mqtt.broker.network.MqttClient;
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
        @NotNull String reason,
        @NotNull String serverReference,
        @NotNull String responseInformation,
        @NotNull String authenticationMethod,
        @NotNull byte[] authenticationData,
        @NotNull Array<StringPair> userProperties
    ) {
        return new ConnectAck5OutPacket(
            client,
            reasonCode,
            sessionPresent,
            requestedClientId,
            requestedSessionExpiryInterval,
            requestedKeepAlive,
            reason,
            serverReference,
            responseInformation,
            authenticationMethod,
            authenticationData,
            userProperties
        );
    }

    @Override
    public @NotNull MqttWritablePacket newPublish(
        @NotNull MqttClient client,
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
            client,
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
        @NotNull MqttClient client,
        int packetId,
        @NotNull PublishAckReasonCode reasonCode,
        @NotNull String reason,
        @NotNull Array<StringPair> userProperties
    ) {
        return new PublishAck5OutPacket(client, packetId, reasonCode, userProperties, reason);
    }

    @Override
    public @NotNull MqttWritablePacket newSubscribeAck(
        @NotNull MqttClient client,
        int packetId,
        @NotNull Array<SubscribeAckReasonCode> reasonCodes,
        @NotNull String reason,
        @NotNull Array<StringPair> userProperties
    ) {
        return new SubscribeAck5OutPacket(client, packetId, reasonCodes, userProperties, reason);
    }

    @Override
    public @NotNull MqttWritablePacket newUnsubscribeAck(
        @NotNull MqttClient client,
        int packetId,
        @NotNull Array<UnsubscribeAckReasonCode> reasonCodes,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        return new UnsubscribeAck5OutPacket(client, packetId, reasonCodes, userProperties, reason);
    }

    @Override
    public @NotNull MqttWritablePacket newDisconnect(
        @NotNull MqttClient client,
        @NotNull DisconnectReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason,
        @NotNull String serverReference
    ) {
        return new Disconnect5OutPacket(client, reasonCode, userProperties, reason, serverReference);
    }

    @Override
    public @NotNull MqttWritablePacket newAuthenticate(
        @NotNull MqttClient client,
        @NotNull AuthenticateReasonCode reasonCode,
        @NotNull String authenticateMethod,
        @NotNull byte[] authenticateData,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        return new Authentication5OutPacket(
            client,
            reasonCode,
            authenticateMethod,
            authenticateData,
            userProperties,
            reason
        );
    }

    @Override
    public @NotNull MqttWritablePacket newPublishRelease(
        @NotNull MqttClient client,
        int packetId,
        @NotNull PublishReleaseReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        return new PublishRelease5OutPacket(client, packetId, reasonCode, userProperties, reason);
    }

    @Override
    public @NotNull MqttWritablePacket newPublishReceived(
        @NotNull MqttClient client,
        int packetId,
        @NotNull PublishReceivedReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        return new PublishReceived5OutPacket(client, packetId, reasonCode, userProperties, reason);
    }

    @Override
    public @NotNull MqttWritablePacket newPublishCompleted(
        @NotNull MqttClient client,
        int packetId,
        @NotNull PublishCompletedReasonCode reasonCode,
        @NotNull Array<StringPair> userProperties,
        @NotNull String reason
    ) {
        return new PublishComplete5OutPacket(client, packetId, reasonCode, userProperties, reason);
    }
}
