package com.ss.mqtt.broker.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.StackWalker.Option;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;
import javasabr.rlib.collections.array.ArrayFactory;
import javasabr.rlib.collections.array.MutableArray;
import javasabr.rlib.common.util.ArrayUtils;
import javasabr.rlib.common.util.ReflectionUtils;

public class DebugUtils {

  public static class PrintOnlyProvidedFields implements ExclusionStrategy {

    private final Class<?> type;
    private final Set<String> fieldNames;

    public PrintOnlyProvidedFields(Class<?> type, String... fieldNames) {
      this.type = type;
      this.fieldNames = Set.of(fieldNames);
    }

    @Override
    public boolean shouldSkipField(FieldAttributes attributes) {
      Class<?> declaringClass = attributes.getDeclaringClass();
      if (declaringClass != type) {
        return false;
      } else {
        return !fieldNames.contains(attributes.getName());
      }
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
      return false;
    }
  }

  private static final MutableArray<ExclusionStrategy> ADDITIONAL_EXCLUDE_STRATEGIES = ArrayFactory.copyOnModifyArray(
      ExclusionStrategy.class);

  private static class ExclusionStrategyContainer implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes attributes) {
      return ADDITIONAL_EXCLUDE_STRATEGIES
          .iterations()
          .anyMatch(attributes, ExclusionStrategy::shouldSkipField);
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
      return ADDITIONAL_EXCLUDE_STRATEGIES
          .iterations()
          .anyMatch(clazz, ExclusionStrategy::shouldSkipClass);
    }
  }

  private static final ExclusionStrategy[] EXCLUSION_STRATEGIES = ArrayUtils.array(new ExclusionStrategyContainer());

  private static final Gson GSON = new GsonBuilder()
      .setPrettyPrinting()
      .setExclusionStrategies(EXCLUSION_STRATEGIES)
      .create();

  public static void registerIncludedFields(String... fieldNames) {

    Class<?> callerClass = StackWalker
        .getInstance(Option.RETAIN_CLASS_REFERENCE)
        .getCallerClass();

    Collection<Field> allFields = ReflectionUtils.getAllDeclaredFields(callerClass);

    for (String fieldName : fieldNames) {

      boolean anyMatch = allFields
          .stream()
          .anyMatch(field -> field
              .getName()
              .equals(fieldName));

      if (!anyMatch) {
        throw new RuntimeException("Not found field " + fieldName + " in type " + callerClass);
      }
    }

    ADDITIONAL_EXCLUDE_STRATEGIES.add(new PrintOnlyProvidedFields(callerClass, fieldNames));
  }

  public static String toJsonString(Object object) {
    return GSON.toJson(object);
  }
}
