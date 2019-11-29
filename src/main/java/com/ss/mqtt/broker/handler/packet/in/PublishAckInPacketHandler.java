package com.ss.mqtt.broker.handler.packet.in;

import com.ss.mqtt.broker.network.packet.in.PublishAckInPacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PublishAckInPacketHandler extends PendingOutResponseInPacketHandler<PublishAckInPacket> {}
