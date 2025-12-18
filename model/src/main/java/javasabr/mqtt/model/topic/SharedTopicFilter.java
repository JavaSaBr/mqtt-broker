package javasabr.mqtt.model.topic;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Accessors(fluent = true, chain = false)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SharedTopicFilter extends TopicFilter {

  public static final String SHARE_KEYWORD = "$share";

  String shareName;

  public SharedTopicFilter(String rawTopicFilter, String shareName) {
    super(rawTopicFilter);
    this.shareName = shareName;
  }

  public static SharedTopicFilter valueOf(String rawSharedTopicFilter) {
    // $share/{ShareName}/{filter}
    int firstSlash = rawSharedTopicFilter.indexOf(DELIMITER) + 1;
    int secondSlash = rawSharedTopicFilter.indexOf(DELIMITER, firstSlash);
    String shareName = rawSharedTopicFilter.substring(firstSlash, secondSlash);
    String rawTopicFilter = rawSharedTopicFilter.substring(secondSlash + 1);
    return new SharedTopicFilter(rawTopicFilter, shareName);
  }

  @Override
  public boolean isShared() {
    return true;
  }

  public static boolean isShared(String rawTopicFilter) {
    return rawTopicFilter.startsWith(SharedTopicFilter.SHARE_KEYWORD);
  }
}

