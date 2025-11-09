package javasabr.mqtt.network.session;

public interface MessageTacker {
  boolean isInUse(int messageId);
  void add(int messageId);
  void remove(int messageId);
}
