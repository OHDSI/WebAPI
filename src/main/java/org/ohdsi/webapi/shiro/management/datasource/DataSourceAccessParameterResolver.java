package org.ohdsi.webapi.shiro.management.datasource;

import org.ohdsi.webapi.shiro.annotations.CcGenerationId;
import org.ohdsi.webapi.shiro.annotations.PathwayAnalysisGenerationId;
import org.ohdsi.webapi.shiro.annotations.SourceId;
import org.ohdsi.webapi.shiro.annotations.SourceKey;
import org.ohdsi.webapi.source.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

@Component
public class DataSourceAccessParameterResolver {

  @Autowired
  private SourceKeyAccessor sourceKeyAccessor;

  @Autowired
  private SourceIdAccessor sourceIdAccessor;

  @Autowired
  private CcGenerationIdAccessor generationIdAccessor;

  @Autowired
  private PathwayAnalysisGenerationIdAccessor pathwayAnalysisGenerationIdAccessor;

  @Autowired
  private SourceAccessor sourceAccessor;

  public <T> AccessorParameterBinding<T> resolveParameterBinding(Method method){
    Annotation[][] annotations = method.getParameterAnnotations();
    for(int i = 0; i < annotations.length; i++) {
      DataSourceAccessor<?> result = null;
      if (isAnnotated(annotations[i], SourceKey.class)) {
        result = sourceKeyAccessor;
      } else if (isAnnotated(annotations[i], SourceId.class)) {
        result = sourceIdAccessor;
      } else if (isAnnotated(annotations[i], CcGenerationId.class)) {
        result = generationIdAccessor;
      } else if (isAnnotated(annotations[i], PathwayAnalysisGenerationId.class)) {
        result = pathwayAnalysisGenerationIdAccessor;
      }
      if (Objects.nonNull(result)) {
        return new AccessorParameterBinding(i, result);
      }
    }
    Class<?>[] parameterTypes = method.getParameterTypes();
    for(int i = 0; i < parameterTypes.length; i++) {
      if (parameterTypes[i].isAssignableFrom(Source.class)) {
        return new AccessorParameterBinding(i, sourceAccessor);
      }
    }
    return null;
  }

  private boolean isAnnotated(Annotation[] annotations, Class<? extends Annotation> annotation) {
    return Arrays.stream(annotations).anyMatch(annotation::isInstance);
  }

}
