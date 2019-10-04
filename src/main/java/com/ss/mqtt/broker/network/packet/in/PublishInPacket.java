package com.ss.mqtt.broker.network.packet.in;

import com.ss.rlib.network.annotation.PacketDescription;

@PacketDescription(id = 3)
public class PublishInPacket extends MqttReadablePacket {}
