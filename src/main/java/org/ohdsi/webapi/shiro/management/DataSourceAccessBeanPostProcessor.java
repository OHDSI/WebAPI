package org.ohdsi.webapi.shiro.management;

import org.aopalliance.intercept.MethodInterceptor;
import org.ohdsi.webapi.shiro.annotations.DataSourceAccess;
import org.ohdsi.webapi.shiro.management.datasource.AccessorParameterBinding;
import org.ohdsi.webapi.shiro.management.datasource.DataSourceAccessParameterResolver;
import org.ohdsi.webapi.util.AnnotationReflectionUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;

public class DataSourceAccessBeanPostProcessor implements BeanPostProcessor {

  private Boolean proxyTargetClass;

  private DataSourceAccessParameterResolver accessParameterResolver;

  public DataSourceAccessBeanPostProcessor(DataSourceAccessParameterResolver accessParameterResolver, Boolean proxyTargetClass) {

    this.accessParameterResolver = accessParameterResolver;
    this.proxyTargetClass = proxyTargetClass;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

    Class type = bean.getClass();
    final List<Method> annotatedMethods = getMethodsAnnotatedWith(type);
    Object result = bean;
    if (!annotatedMethods.isEmpty()) {
      ProxyFactory factory = new ProxyFactory(bean);
      factory.setProxyTargetClass(proxyTargetClass);
      factory.addAdvice((MethodInterceptor) invocation -> {
        Method method = invocation.getMethod();
        Object[] args = invocation.getArguments();
        Optional<Method> originalAnnotatedMethod = annotatedMethods.stream().filter(m ->
            Objects.equals(m.getName(), method.getName())
            && Objects.equals(m.getReturnType(), method.getReturnType())
            && Arrays.equals(m.getParameterTypes(), method.getParameterTypes())).findFirst();
        if (originalAnnotatedMethod.isPresent()) {
            AccessorParameterBinding<Object> binding = accessParameterResolver.resolveParameterBinding(originalAnnotatedMethod.get());
            if (Objects.nonNull(binding)) {
              Object value = args[binding.getParameterIndex()];
              binding.getDataSourceAccessor().checkAccess(value);
            }
        }
        return method.invoke(bean, args);
      });
      result = factory.getProxy();
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
