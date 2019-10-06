package com.ss.mqtt.broker.network.packet.in;

import com.ss.mqtt.broker.network.MqttConnection;
import com.ss.rlib.network.packet.impl.AbstractReadablePacket;

public class MqttReadablePacket extends AbstractReadablePacket<MqttConnection> {

    protected MqttReadablePacket(byte info) {
    }
}
