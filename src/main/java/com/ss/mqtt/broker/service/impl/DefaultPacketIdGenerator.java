package com.ss.mqtt.broker.service.impl;

import com.ss.mqtt.broker.model.MqttPropertyConstants;
import com.ss.mqtt.broker.service.PacketIdGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class DefaultPacketIdGenerator implements PacketIdGenerator {

    private final @NotNull AtomicInteger generator;

    public DefaultPacketIdGenerator() {
        this.generator = new AtomicInteger(0);
    }

    @Override
    public int nextPacketId() {

        int nextId = generator.incrementAndGet();

        if (nextId > MqttPropertyConstants.PACKET_ID_FOR_QOS_0) {
            generator.compareAndSet(nextId, 0);
            return nextPacketId();
        }

        return nextId;
    }
}
