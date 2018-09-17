package org.ohdsi.webapi.shiro.management;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shiro.SecurityUtils;
import org.ohdsi.webapi.cohortcharacterization.annotations.CcGenerationId;
import org.ohdsi.webapi.cohortcharacterization.annotations.DataSourceAccess;
import org.ohdsi.webapi.cohortcharacterization.annotations.SourceKey;
import org.ohdsi.webapi.cohortcharacterization.domain.CcGenerationEntity;
import org.ohdsi.webapi.cohortcharacterization.repository.CcGenerationEntityRepository;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DataSourceAccessBeanPostProcessor implements BeanPostProcessor {

  private SourceRepository sourceRepository;

  private CcGenerationEntityRepository ccGenerationRepository;

  public DataSourceAccessBeanPostProcessor(SourceRepository sourceRepository, CcGenerationEntityRepository ccGenerationRepository) {

    this.sourceRepository = sourceRepository;
    this.ccGenerationRepository = ccGenerationRepository;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

    Class type = bean.getClass();
    final List<Method> methods = getMethodsAnnotatedWith(type);
    Object result = bean;
    if (!methods.isEmpty()) {
      result = Proxy.newProxyInstance(type.getClassLoader(), type.getInterfaces(), (proxy, method, args) -> {

        if (methods.stream().anyMatch(m -> Objects.equals(m.getName(), method.getName()))) {
          Optional<Method> targetMethod = methods.stream().filter(m -> Objects.nonNull(findMethod(m, method))).findFirst();
          if (targetMethod.isPresent()) {
            Pair<Integer, Consumer<?>> handler = mapParameters(targetMethod.get());
            if (Objects.nonNull(handler)) {
              Object value = args[handler.getLeft()];
              Consumer<Object> consumer = (Consumer<Object>) handler.getRight();
              consumer.accept(value);
            }
          }
        }
        return method.invoke(bean, args);
      });
    }
    return result;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

    return bean;
  }

  private Method findMethod(Method m1, Method m2) {
    if (m2.getDeclaringClass().isAssignableFrom(m2.getDeclaringClass())) {
      if (Objects.equals(m1.getName(), m2.getName())) {
        return ReflectionUtils.findMethod(m1.getDeclaringClass(), m1.getName(), m1.getParameterTypes());
      }
    }
    return null;
  }

  private List<Method> getMethodsAnnotatedWith(final Class<?> type) {
    List<Method> methods = new ArrayList<>();
    Class<?> current = type;
    while(Objects.nonNull(current) && current != Object.class) {
      methods.addAll(Arrays.stream(current.getMethods())
              .filter(method -> method.isAnnotationPresent(DataSourceAccess.class))
              .collect(Collectors.toList()));
      current = current.getSuperclass();
    }
    methods.forEach(m -> {
      if (Objects.isNull(mapParameters(m))) {
        throw new BeanInitializationException(String.format("One of method: %s parameters should be annotated with SourceKey of CcGenerationId", m.toString()));
      }
    });
    return methods;
  }

  private Pair<Integer, Consumer<?>> mapParameters(Method method){

    Annotation[][] annotations = method.getParameterAnnotations();
    for(int i = 0; i < annotations.length; i++) {
      Consumer<?> result = null;
      if (isAnnotated(annotations[i], SourceKey.class)) {
        result = (Consumer<String>) sourceKey -> {
          Source source = sourceRepository.findBySourceKey(sourceKey);
          checkSourceAccess(source);
        };
      } else if (isAnnotated(annotations[i], CcGenerationId.class)) {
        result = (Consumer<Long>) id -> {
          CcGenerationEntity generationEntity = ccGenerationRepository.findById(id).orElseThrow(NotFoundException::new);
          checkSourceAccess(generationEntity.getSource());
        };
      }
      if (Objects.nonNull(result)) {
        return new ImmutablePair<>(i, result);
      }
    }
    return null;
  }

  private boolean isAnnotated(Annotation[] annotations, Class<? extends Annotation> annotation) {
    return Arrays.stream(annotations).anyMatch(annotation::isInstance);
  }

  private void checkSourceAccess(Source source) {
    if (!SecurityUtils.getSubject().isPermitted(String.format(Security.SOURCE_ACCESS_PERMISSION, source.getSourceKey()))){
      throw new ForbiddenException();
    }
  }

}
