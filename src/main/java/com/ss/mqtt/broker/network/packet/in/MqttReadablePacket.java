package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.rlib.common.util.ClassUtils;
import com.ss.rlib.network.packet.impl.AbstractIdBasedReadablePacket;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public class MqttReadablePacket extends AbstractIdBasedReadablePacket<MqttConnection, MqttReadablePacket> {

    private final Constructor<? extends MqttReadablePacket> constructor;

    public MqttReadablePacket() {
        this.constructor = ClassUtils.getConstructor(getClass(), byte.class);
    }

    public MqttReadablePacket(byte info) {
        this.constructor = null;
    }

    public @NotNull MqttReadablePacket newInstance(byte info) {
        return constructor == null ? ClassUtils.newInstance(getClass()) : ClassUtils.newInstance(constructor, info);
    }
}
