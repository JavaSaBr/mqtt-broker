package javasabr.mqtt.model;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javasabr.mqtt.base.util.DebugUtils;
import javasabr.rlib.collections.dictionary.DictionaryFactory;
import javasabr.rlib.collections.dictionary.LockableRefToRefDictionary;
import org.jspecify.annotations.Nullable;

public abstract class AbstractTrieNode<T> {

  static {
    DebugUtils.registerIncludedFields("childNodes");
  }
  
  @Nullable
  protected volatile LockableRefToRefDictionary<String, T> childNodes;

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
    LockableRefToRefDictionary<String, T> childNodes = getOrCreateChildNodes();
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

  protected void collectChildNodes(Collection<T> resultCollection) {
    var localChildNodes = childNodes;
    if (localChildNodes == null) {
      return;
    }
    long stamp = localChildNodes.readLock();
    try {
      localChildNodes.values(resultCollection);
    } finally {
      localChildNodes.readUnlock(stamp);
    }
  }

  @Nullable
  protected Collection<T> getChildNodes(Supplier<Collection<T>> resultCollectionFactory) {
    var localChildNodes = childNodes;
    if (localChildNodes == null) {
      return null;
    }
    Collection<T> resultCollection = resultCollectionFactory.get();
    collectChildNodes(resultCollection);
    return resultCollection;
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
  
  @JsonValue
  Object jsonDebugValue() {
    Map<String, Object> result = new HashMap<>(1);
    result.put("childNodes", childNodes);
    return result;
  }
  
  @Override
  public String toString() {
    return DebugUtils.toJsonString(this);
  }
}
