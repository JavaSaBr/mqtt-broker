package javasabr.mqtt.broker.application.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

public class NativeConfigurationHints implements RuntimeHintsRegistrar {

  private static final String[] REFLECTION_PACKAGES = {
      "javasabr.mqtt",
      };

  private static final String[] ARRAY_PACKAGES = {
      "javasabr.mqtt",
      "javasabr.rlib",
      };

  private static final String[] JDK_ARRAY_TYPES = {
      "java.util.function.Consumer[]",
      "java.util.function.BiConsumer[]",
      "java.nio.ByteBuffer[]",
      "reactor.core.publisher.FluxSink[]",
      "java.lang.String[]",
      "java.util.UUID[]",
      };

  @Override
  public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
    var scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AssignableTypeFilter(Object.class));

    for (String pkg : REFLECTION_PACKAGES) {
      for (BeanDefinition candidate : scanner.findCandidateComponents(pkg)) {
        hints
            .reflection()
            .registerType(
                TypeReference.of(candidate.getBeanClassName()),
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.ACCESS_DECLARED_FIELDS);
      }
    }

    var arrayScanner = new ClassPathScanningCandidateComponentProvider(false);
    arrayScanner.addIncludeFilter(new AssignableTypeFilter(Object.class));

    for (String pkg : ARRAY_PACKAGES) {
      for (var candidate : arrayScanner.findCandidateComponents(pkg)) {
        hints
            .reflection()
            .registerType(TypeReference.of(candidate.getBeanClassName() + "[]"));
      }
    }

    for (String arrayType : JDK_ARRAY_TYPES) {
      hints
          .reflection()
          .registerType(TypeReference.of(arrayType));
    }
  }
}
