package javasabr.mqtt.legacy.handler.packet.in;

import javasabr.mqtt.legacy.network.packet.in.PublishReceivedInPacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PublishReceiveInPacketHandler extends PendingOutResponseInPacketHandler<PublishReceivedInPacket> {}
