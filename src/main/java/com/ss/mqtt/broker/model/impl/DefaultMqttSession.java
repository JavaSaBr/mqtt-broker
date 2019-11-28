package com.ss.mqtt.broker.model.impl;

import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.model.MqttSession.UnsafeMqttSession;
import com.ss.mqtt.broker.model.SubscribeTopicFilter;
import com.ss.mqtt.broker.model.topic.TopicFilter;
import com.ss.mqtt.broker.network.client.MqttClient;
import com.ss.mqtt.broker.network.packet.HasPacketId;
import com.ss.mqtt.broker.network.packet.in.PublishInPacket;
import com.ss.rlib.common.function.NotNullTripleConsumer;
import com.ss.rlib.common.util.ClassUtils;
import com.ss.rlib.common.util.NumberUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ConcurrentArray;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@ToString(of = "clientId")
@EqualsAndHashCode(of = "clientId")
public class DefaultMqttSession implements UnsafeMqttSession {

    @Getter
    @AllArgsConstructor
    private static class PendingPublish {

        private final @NotNull PublishInPacket publish;
        private final @NotNull PendingPacketHandler handler;
        private final long registeredTime;
        private final int packetId;

        private volatile long lastAttemptTime;
    }

    private static void removeExpiredPackets(@NotNull Array<PendingPublish> publishes) {

        var currentTime = System.currentTimeMillis();
        var array = publishes.array();

        for (int i = 0, length = publishes.size(); i < length; i++) {

            var pendingPublish = array[i];

            var publish = pendingPublish.publish;
            var messageExpiryInterval = publish.getMessageExpiryInterval();

            if (messageExpiryInterval == MqttPropertyConstants.MESSAGE_EXPIRY_INTERVAL_UNDEFINED ||
                messageExpiryInterval == MqttPropertyConstants.MESSAGE_EXPIRY_INTERVAL_INFINITY) {
                continue;
            }

            var expiredTime = pendingPublish.registeredTime + (messageExpiryInterval * 1000);

            if (expiredTime < currentTime) {
                log.debug("Remove pending publish {} by expiration reason", publish);
                publishes.fastRemove(i);
                i--;
                length--;
            }
        }
    }

    private static void registerPublish(
        @NotNull PublishInPacket publish,
        @NotNull PendingPacketHandler handler,
        int packetId,
        @NotNull ConcurrentArray<PendingPublish> pendingPublishes
    ) {

        var currentTime = System.currentTimeMillis();
        var pendingPublish = new PendingPublish(publish, handler, currentTime, packetId, currentTime);

        pendingPublishes.runInWriteLock(pendingPublish, Array::add);
    }

    private void updatePendingPacket(
        @NotNull MqttClient client,
        @NotNull HasPacketId response,
        @NotNull ConcurrentArray<PendingPublish> pendingPublishes,
        @NotNull String clientId
    ) {

        var packetId = response.getPacketId();
        var pendingPublish = pendingPublishes.findAnyConvertedToIntInReadLock(
            packetId,
            PendingPublish::getPublish,
            PublishInPacket::getPacketId,
            NumberUtils::equals
        );

        if (pendingPublish == null) {
            log.warn("Not found pending publish for client {} by received packet {}", clientId, response);
            return;
        }

        var shouldBeRemoved = pendingPublish.handler.handleResponse(client, response);

        if (shouldBeRemoved) {
            pendingPublishes.runInWriteLock(pendingPublish, Array::fastRemove);
        }
    }

    private final @NotNull String clientId;
    private final @NotNull ConcurrentArray<PendingPublish> pendingOutPublishes;
    private final @NotNull ConcurrentArray<PendingPublish> pendingInPublishes;
    private final @NotNull AtomicInteger packetIdGenerator;
    private final @NotNull ConcurrentArray<SubscribeTopicFilter> topicFilters;

    private volatile @Getter @Setter long expirationTime = -1;

    public DefaultMqttSession(@NotNull String clientId) {
        this.clientId = clientId;
        this.pendingOutPublishes = ConcurrentArray.ofType(PendingPublish.class);
        this.pendingInPublishes = ConcurrentArray.ofType(PendingPublish.class);
        this.packetIdGenerator = new AtomicInteger(0);
        this.topicFilters = ConcurrentArray.ofType(SubscribeTopicFilter.class);
    }

    @Override
    public int nextPacketId() {

        var nextId = packetIdGenerator.incrementAndGet();

        if (nextId >= MqttPropertyConstants.MAXIMUM_PACKET_ID) {
            packetIdGenerator.compareAndSet(nextId, 0);
            return nextPacketId();
        }

        return nextId;
    }

    @Override
    public @NotNull String getClientId() {
        return clientId;
    }

    @Override
    public void registerOutPublish(@NotNull PublishInPacket publish,
        @NotNull PendingPacketHandler handler,
        int packetId
    ) {
        registerPublish(publish, handler, packetId, pendingOutPublishes);
    }

    @Override
    public void registerInPublish(
        @NotNull PublishInPacket publish,
        @NotNull PendingPacketHandler handler,
        int packetId
    ) {
        registerPublish(publish, handler, packetId, pendingInPublishes);
    }

    @Override
    public boolean hasOutPending() {
        return !pendingOutPublishes.isEmpty();
    }

    @Override
    public boolean hasInPending() {
        return !pendingInPublishes.isEmpty();
    }

    @Override
    public boolean hasOutPending(int packetId) {
        return pendingOutPublishes.findAnyConvertedToIntInReadLock(
            packetId,
            PendingPublish::getPublish,
            PublishInPacket::getPacketId,
            NumberUtils::equals
        ) != null;
    }

    @Override
    public boolean hasInPending(int packetId) {
        return pendingInPublishes.findAnyConvertedToIntInReadLock(
            packetId,
            PendingPublish::getPublish,
            PublishInPacket::getPacketId,
            NumberUtils::equals
        ) != null;
    }

    @Override
    public void removeExpiredPackets() {
        if (!pendingOutPublishes.isEmpty()) {
            pendingOutPublishes.runInWriteLock(DefaultMqttSession::removeExpiredPackets);
        }
    }

    @Override
    public void resendPendingPacketsAsync(@NotNull MqttClient client, int retryInterval) {
        var currentTime = System.currentTimeMillis();
        var stamp = pendingOutPublishes.readLock();
        try {

            for (var pendingPublish : pendingOutPublishes) {

                if (currentTime - pendingPublish.lastAttemptTime <= retryInterval) {
                    continue;
                }

                log.debug("Re-try to send publish {}", pendingPublish.publish);

                pendingPublish.lastAttemptTime = currentTime;
                pendingPublish.handler.retryAsync(client, pendingPublish.publish, pendingPublish.packetId);
            }

        } finally {
            pendingOutPublishes.readUnlock(stamp);
        }
    }

    @Override
    public void updateOutPendingPacket(@NotNull MqttClient client, @NotNull HasPacketId response) {
        updatePendingPacket(client, response, pendingOutPublishes, clientId);
    }

    @Override
    public void updateInPendingPacket(@NotNull MqttClient client, @NotNull HasPacketId response) {
        updatePendingPacket(client, response, pendingInPublishes, clientId);
    }

    @Override
    public <F, S> void forEachTopicFilter(
        @NotNull F first,
        @NotNull S second,
        @NotNull NotNullTripleConsumer<F, S, SubscribeTopicFilter> consumer
    ) {
        topicFilters.forEachInReadLock(first, second, consumer);
    }

    @Override
    public void addSubscriber(@NotNull SubscribeTopicFilter subscribe) {
        topicFilters.runInWriteLock(subscribe, Collection::add);
    }

    @Override
    public void removeSubscriber(@NotNull TopicFilter topicFilter) {
        topicFilters.removeIfConvertedInWriteLock(
            topicFilter,
            SubscribeTopicFilter::getTopicFilter,
            Object::equals
        );
    }

    @Override
    public void clear() {
        pendingOutPublishes.runInWriteLock(Collection::clear);
    }
}
