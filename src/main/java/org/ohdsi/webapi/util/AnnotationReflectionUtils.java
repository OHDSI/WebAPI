package org.ohdsi.webapi.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AnnotationReflectionUtils {

  public static List<Method> getMethodsAnnotatedWith(final Class<?> type, final Class<? extends Annotation> annotation) {
    List<Method> methods = new ArrayList<>();
    Class<?> current = type;
    while (Objects.nonNull(current) && current != Object.class) {
      methods.addAll(Arrays.stream(current.getMethods())
              .filter(method -> method.isAnnotationPresent(annotation))
              .collect(Collectors.toList()));
      current = current.getSuperclass();
    }
    return methods;
  }

}
