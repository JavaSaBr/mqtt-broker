package com.ss.mqtt.broker.handler.packet.in;

import com.ss.mqtt.broker.network.packet.in.PublishCompleteInPacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PublishCompleteInPacketHandler extends PendingOutResponseInPacketHandler<PublishCompleteInPacket> {}
