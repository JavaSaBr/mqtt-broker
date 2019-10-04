package com.ss.mqtt.broker.network.packet.in;

import com.ss.rlib.network.annotation.PacketDescription;

@PacketDescription(id = 10)
public class UnsubscribeInPacket extends MqttReadablePacket {}
