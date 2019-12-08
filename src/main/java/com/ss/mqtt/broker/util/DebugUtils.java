package com.ss.mqtt.broker.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ss.rlib.common.util.ReflectionUtils;
import com.ss.rlib.common.util.array.Array;
import com.ss.rlib.common.util.array.ArrayFactory;
import org.jetbrains.annotations.NotNull;

import java.lang.StackWalker.Option;
import java.lang.reflect.Field;
import java.util.Set;

public class DebugUtils {

    public static class PrintOnlyProvidedFields implements ExclusionStrategy {

        private final @NotNull Class<?> type;
        private final @NotNull Set<String> fieldNames;

        public PrintOnlyProvidedFields(@NotNull Class<?> type, @NotNull String... fieldNames) {
            this.type = type;
            this.fieldNames = Set.of(fieldNames);
        }

        @Override
        public boolean shouldSkipField(@NotNull FieldAttributes attributes) {

            var declaringClass = attributes.getDeclaringClass();

            if (declaringClass != type) {
                return false;
            } else {
                return !fieldNames.contains(attributes.getName());
            }
        }

        @Override
        public boolean shouldSkipClass(@NotNull Class<?> clazz) {
            return false;
        }
    }

    private static final Array<ExclusionStrategy> ADDITIONAL_EXCLUDE_STRATEGIES =
        ArrayFactory.newCopyOnModifyArray(ExclusionStrategy.class);

    private static class ExclusionStrategyContainer implements ExclusionStrategy {

        @Override
        public boolean shouldSkipField(@NotNull FieldAttributes attributes) {
            return ADDITIONAL_EXCLUDE_STRATEGIES.anyMatchR(attributes, ExclusionStrategy::shouldSkipField);
        }

        @Override
        public boolean shouldSkipClass(@NotNull Class<?> clazz) {
            return ADDITIONAL_EXCLUDE_STRATEGIES.anyMatchR(clazz, ExclusionStrategy::shouldSkipClass);
        }
    }

    private static final ExclusionStrategy[] EXCLUSION_STRATEGIES = ArrayFactory.toArray(
        new ExclusionStrategyContainer()
    );

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .setExclusionStrategies(EXCLUSION_STRATEGIES)
        .create();

    public static void registerIncludedFields(@NotNull String... fieldNames) {

        var callerClass = StackWalker
            .getInstance(Option.RETAIN_CLASS_REFERENCE)
            .getCallerClass();

        var allFields = ReflectionUtils.getAllDeclaredFields(callerClass);

        for (var fieldName : fieldNames) {
            if (!allFields.anyMatchConverted(fieldName, Field::getName, String::equals)) {
                throw new RuntimeException("Not found field " + fieldName + " in type " + callerClass);
            }
        }

        ADDITIONAL_EXCLUDE_STRATEGIES.add(new PrintOnlyProvidedFields(callerClass, fieldNames));
    }

    public static @NotNull String toJsonString(@NotNull Object object) {
        return GSON.toJson(object);
    }
}
