package javasabr.mqtt.model;

import java.util.function.Supplier;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

@Getter
@Accessors
public abstract class AbstractTrieNode<T> {

  @Nullable
  volatile LockableRefToRefDictionary<String, T> childNodes;

  protected abstract Supplier<T> getNodeFactory();

  private LockableRefToRefDictionary<String, T> getOrCreateChildNodes() {
    var current = childNodes;
    if (current != null) {
      return current;
    }
    synchronized (this) {
      current = childNodes;
      if (current == null) {
        current = DictionaryFactory.stampedLockBasedRefToRefDictionary();
        childNodes = current;
      }
      return current;
    }
  }

  protected T getOrCreateChildNode(String segment) {
    var childNodes = getOrCreateChildNodes();
    long stamp = childNodes.readLock();
    try {
      T topicFilterNode = childNodes.get(segment);
      if (topicFilterNode != null) {
        return topicFilterNode;
      }
    } finally {
      childNodes.readUnlock(stamp);
    }
    stamp = childNodes.writeLock();
    try {
      return childNodes.getOrCompute(segment, getNodeFactory());
    } finally {
      childNodes.writeUnlock(stamp);
    }
  }

  @Nullable
  protected T getChildNode(String segment) {
    var localChildNodes = childNodes;
    if (localChildNodes == null) {
      return null;
    }
    long stamp = localChildNodes.readLock();
    try {
      return localChildNodes.get(segment);
    } finally {
      localChildNodes.readUnlock(stamp);
    }
  }

  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }
}
