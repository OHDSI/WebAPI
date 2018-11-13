package org.ohdsi.webapi.shiro.management;

import org.ohdsi.webapi.shiro.annotations.DataSourceAccess;
import org.ohdsi.webapi.shiro.management.datasource.AccessorParameterBinding;
import org.ohdsi.webapi.shiro.management.datasource.DataSourceAccessParameterResolver;
import org.ohdsi.webapi.util.AnnotationReflectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class DataSourceAccessBeanPostProcessor implements BeanPostProcessor {

  private DataSourceAccessParameterResolver accessParameterResolver;

  public DataSourceAccessBeanPostProcessor(DataSourceAccessParameterResolver accessParameterResolver) {

    this.accessParameterResolver = accessParameterResolver;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

    Class type = bean.getClass();
    final List<Method> methods = getMethodsAnnotatedWith(type);
    Object result = bean;
    if (!methods.isEmpty()) {
      result = Proxy.newProxyInstance(type.getClassLoader(), type.getInterfaces(), (proxy, method, args) -> {

        if (methods.stream().anyMatch(m -> Objects.equals(m.getName(), method.getName()))) {
          Optional<Method> targetMethod = methods.stream().filter(m -> Objects.nonNull(findMethod(m, method))).findFirst();
          if (targetMethod.isPresent()) {
            AccessorParameterBinding<Object> binding = accessParameterResolver.resolveParameterBinding(targetMethod.get());
            if (Objects.nonNull(binding)) {
              Object value = args[binding.getParameterIndex()];
              binding.getDataSourceAccessor().checkAccess(value);
            }
          }
        }
        return method.invoke(bean, args);
      });
    }
    return result;
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
    List<Method> methods = AnnotationReflectionUtils.getMethodsAnnotatedWith(type, DataSourceAccess.class);
    methods.forEach(m -> {
      if (Objects.isNull(accessParameterResolver.resolveParameterBinding(m))) {
        throw new BeanInitializationException(String.format("One of method: %s parameters should be annotated with SourceKey of CcGenerationId", m.toString()));
      }
    });
    return methods;
  }


}
