package javasabr.mqtt.service.handler.in;

import javasabr.mqtt.network.packet.in.PublishReceivedInPacket;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PublishReceiveInPacketHandler extends PendingOutResponseInPacketHandler<PublishReceivedInPacket> {}
