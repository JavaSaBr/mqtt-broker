package javasabr.mqtt.network;

import java.nio.channels.AsynchronousSocketChannel;
import javasabr.rlib.network.Network;

public interface MqttConnectionFactory {

  MqttConnection newConnection(Network<MqttConnection> network, AsynchronousSocketChannel channel);
}
