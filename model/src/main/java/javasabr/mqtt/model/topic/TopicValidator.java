package javasabr.mqtt.model.topic;

public class TopicValidator {

  private static final String DOUBLE_DELIMITER = TopicName.DELIMITER.repeat(2);
  private static final String DOUBLE_SINGLE_WILDCARD = TopicFilter.SINGLE_LEVEL_WILDCARD.repeat(2);
  private static final String NULL_CHAR = "\u0000";

  /**
   * Checks that shared topic filters contains all valid parts.
   * <pre>{@code
   *    $share/{ShareName}/{filter}
   * }</pre>
   */
  public static boolean validateSharedTopicFilter(String rawSharedTopicFilter) {
    // $share/{ShareName}/{filter}
    String[] parts = rawSharedTopicFilter.split(AbstractTopic.DELIMITER, 3);
    if (parts.length != 3) {
      return false;
    } else if (!SharedTopicFilter.SHARE_KEYWORD.equals(parts[0])) {
      return false;
    } else if (!baseShareNameValidation(parts[1])) {
      return false;
    }
    String rawTopicFilter = parts[2];
    return baseTopicValidation(rawTopicFilter)
        && baseMultiWildcardTopicFilterValidation(rawTopicFilter)
        && baseSingleWildcardTopicFilterValidation(rawTopicFilter);
  }

  public static boolean validateTopicFilter(String rawTopicFilter) {
    return baseTopicValidation(rawTopicFilter)
        && baseMultiWildcardTopicFilterValidation(rawTopicFilter)
        && baseSingleWildcardTopicFilterValidation(rawTopicFilter);
  }

  public static boolean validateTopicName(String rawTopicName) {
    return baseTopicValidation(rawTopicName) && baseTopicNameValidation(rawTopicName);
  }

  private static boolean baseTopicValidation(String rawTopic) {
    return !rawTopic.isEmpty()
        && !rawTopic.contains(NULL_CHAR)
        && !rawTopic.contains(DOUBLE_DELIMITER);
  }

  /**
   * Checks that topic name doesn't any special topic filter chars.
   */
  private static boolean baseTopicNameValidation(String rawTopicName) {
    return !rawTopicName.contains(TopicFilter.MULTI_LEVEL_WILDCARD)
        && !rawTopicName.contains(TopicFilter.SINGLE_LEVEL_WILDCARD);
  }

  /**
   * Checks that share name doesn't any special chars.
   */
  private static boolean baseShareNameValidation(String rawTopicName) {
    return !rawTopicName.contains(TopicFilter.MULTI_LEVEL_WILDCARD)
        && !rawTopicName.contains(TopicFilter.SINGLE_LEVEL_WILDCARD)
        && !rawTopicName.contains(AbstractTopic.DELIMITER);
  }

  /**
   * Checks that if topic filter contains multi level wildcard that it's in valid way:
   * <pre>{@code
   *    '#'
   *    '/#'
   *    '/segment1/#'
   * }</pre>
   */
  private static boolean baseMultiWildcardTopicFilterValidation(String rawTopicFilter) {
    int index = rawTopicFilter.indexOf(TopicFilter.MULTI_LEVEL_WILDCARD_CHAR);
    if (index < 0) {
      return true;
    }
    // for the case '#'
    int length = rawTopicFilter.length();
    if (length == 1) {
      return true;
    }
    char leftChar = rawTopicFilter.charAt(index - 1);
    if (leftChar != AbstractTopic.DELIMITER_CHAR) {
      // before '#' always should be delimiter
      return false;
    }
    // '/segment1/segment2/#' should be always in the end of topic filter
    return index == length - 1;
  }

  /**
   * Checks that if topic filter contains multi level wildcard that it's in valid way:
   * <pre>{@code
   *    '+'
   *    '+/+'
   *    '/+'
   *    '+/segment1/#'
   *    'segment1/+/segment3'
   *    '+/segment2/+/segment4'
   *    '+/segment2/+/segment4/+'
   * }</pre>
   */
  private static boolean baseSingleWildcardTopicFilterValidation(String rawTopicFilter) {
    if (!rawTopicFilter.contains(TopicFilter.SINGLE_LEVEL_WILDCARD)) {
      return true;
    }
    // we don't allow '++' combination
    else if (rawTopicFilter.contains(DOUBLE_SINGLE_WILDCARD)) {
      return false;
    }
    // just '+'
    else if (rawTopicFilter.equals(TopicFilter.SINGLE_LEVEL_WILDCARD)) {
      return true;
    }

    int lastIndex = rawTopicFilter.length() - 1;
    for (int i = 0, length = lastIndex + 1; i < length; i++) {
      char ch = rawTopicFilter.charAt(i);
      if (ch != TopicFilter.SINGLE_LEVEL_WILDCARD_CHAR) {
        continue;
      }

      // for the first char we should check only the right char
      if (i == 0) {
        char rightChar = rawTopicFilter.charAt(i + 1);
        if (rightChar != AbstractTopic.DELIMITER_CHAR) {
          return false;
        }
        // we already checked the right char
        i++;
      }
      // for the last char we should check only the left char
      else if (i == lastIndex) {
        char leftChar = rawTopicFilter.charAt(i - 1);
        if (leftChar != AbstractTopic.DELIMITER_CHAR) {
          return false;
        }
      }
      // check that the left and the right chars are '/'
      else {
        char leftChar = rawTopicFilter.charAt(i - 1);
        if (leftChar != AbstractTopic.DELIMITER_CHAR) {
          return false;
        }
        char rightChar = rawTopicFilter.charAt(i + 1);
        if (rightChar != AbstractTopic.DELIMITER_CHAR) {
          return false;
        }
        // we already checked the right char
        i++;
      }
    }

    return true;
  }
}
