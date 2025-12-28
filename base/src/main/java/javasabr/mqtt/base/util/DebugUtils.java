package javasabr.mqtt.base.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFilter;
import java.lang.StackWalker.Option;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javasabr.rlib.collections.array.Array;
import javasabr.rlib.collections.dictionary.RefToRefDictionary;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.PropertyFilter;
import tools.jackson.databind.ser.PropertyWriter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;
import tools.jackson.databind.ser.std.StdSerializer;

public class DebugUtils {

  @JsonFilter("debugFieldsFilter")
  public static class DebugFieldsFilterMixIn {}

  private static final ConcurrentMap<Class<?>, Set<String>> INCLUDED_DEBUG_PROPERTIES =
      new ConcurrentHashMap<>();


  private static class IncludeDebugFieldsPropertyFilter implements PropertyFilter {

    @Override
    public void serializeAsProperty(
        Object pojo,
        JsonGenerator jsonGenerator,
        SerializationContext context,
        PropertyWriter writer)
        throws Exception {

      AnnotatedMember member = writer.getMember();
      String name = member.getName();
      Class<?> declaringClass = member.getDeclaringClass();
      Set<String> fields = INCLUDED_DEBUG_PROPERTIES.get(declaringClass);

      while (declaringClass != Object.class && fields == null) {
        declaringClass = declaringClass.getSuperclass();
        fields = INCLUDED_DEBUG_PROPERTIES.get(declaringClass);
      }

      if (fields == null || fields.contains(name)) {
        try {
          writer.serializeAsProperty(pojo, jsonGenerator, context);
        } catch (IllegalAccessException ignore) {
          // ignore
        }
      }
    }

    @Override
    public void serializeAsElement(
        Object elementValue,
        JsonGenerator jsonGenerator,
        SerializationContext context,
        PropertyWriter writer) throws Exception {
      writer.serializeAsElement(elementValue, jsonGenerator, context);
    }

    @Override
    public void depositSchemaProperty(
        PropertyWriter writer,
        JsonObjectFormatVisitor objectFormatVisitor,
        SerializationContext context) {
      writer.depositSchemaProperty(objectFormatVisitor, context);
    }

    @Override
    public PropertyFilter snapshot() {
      return this;
    }
  }

  public static class ArraySerializer extends StdSerializer<Array<?>> {

    public ArraySerializer() {
      super(Array.class);
    }

    @Override
    public void serialize(
        Array<?> array,
        JsonGenerator gen,
        SerializationContext provider) throws JacksonException {
      gen.writeStartArray();
      for (Object element : array) {
        gen.writePOJO(element);
      }
      gen.writeEndArray();
    }
  }

  public static class RefToRefDictionarySerializer extends StdSerializer<RefToRefDictionary<?, ?>> {

    public RefToRefDictionarySerializer() {
      super(RefToRefDictionary.class);
    }

    @Override
    public void serialize(
        RefToRefDictionary<?, ?> dictionary,
        JsonGenerator gen,
        SerializationContext provider) throws JacksonException {
      gen.writeStartObject();
      dictionary.forEach((key, value) -> gen.writePOJOProperty(key.toString(), value));
      gen.writeEndObject();
    }
  }

  private static final SimpleFilterProvider DEBUG_FIELDS_FILTER = new SimpleFilterProvider();
  private static final IncludeDebugFieldsPropertyFilter INCLUDE_DEBUG_FIELDS_PROPERTY_FILTER =
      new IncludeDebugFieldsPropertyFilter();

  static {
    DEBUG_FIELDS_FILTER.addFilter("debugFieldsFilter", INCLUDE_DEBUG_FIELDS_PROPERTY_FILTER);
  }

  private static final JsonMapper DEBUG_OBJECT_MAPPER = JsonMapper
      .builder()
      .enable(SerializationFeature.INDENT_OUTPUT)
      .changeDefaultVisibility(visibilityChecker -> visibilityChecker
          .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
          .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
          .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE))
      .addMixIn(Object.class, DebugFieldsFilterMixIn.class)
      .addModule(new SimpleModule()
          .addSerializer(new ArraySerializer())
          .addSerializer(new RefToRefDictionarySerializer()))
      .filterProvider(DEBUG_FIELDS_FILTER)
      .defaultPrettyPrinter(new DefaultPrettyPrinter()
          .withObjectIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE)
          .withArrayIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE))
      .build();

  public static void registerIncludedFields(String... fieldNames) {

    Class<?> callerClass = StackWalker
        .getInstance(Option.RETAIN_CLASS_REFERENCE)
        .getCallerClass();

    INCLUDED_DEBUG_PROPERTIES.put(callerClass, Set.of(fieldNames));
  }

  public static void registerIncludedFields(Class<?> callerClass, String... fieldNames) {
    INCLUDED_DEBUG_PROPERTIES.put(callerClass, Set.of(fieldNames));
  }

  public static String toJsonString(Object object) {
    return DEBUG_OBJECT_MAPPER
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(object);
  }
}
