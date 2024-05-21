package org.ohdsi.webapi.achilles.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.ohdsi.webapi.achilles.domain.AchillesCacheEntity;
import org.ohdsi.webapi.achilles.service.AchillesCacheService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Aspect
@Component
public class AchillesCacheAspect {
    private static final Logger LOG = LoggerFactory.getLogger(AchillesCacheAspect.class);

    private final SourceRepository sourceRepository;
    private final ObjectMapper objectMapper;
    private final AchillesCacheService cacheService;

    public AchillesCacheAspect(SourceRepository sourceRepository,
                               ObjectMapper objectMapper,
                               AchillesCacheService cacheService) {
        this.sourceRepository = sourceRepository;
        this.objectMapper = objectMapper;
        this.cacheService = cacheService;
    }

    @Pointcut("@annotation(AchillesCache)")
    public void cachePointcut() {
    }

    @Around("cachePointcut()")
    public Object cache(ProceedingJoinPoint joinPoint) throws Throwable {
        String cacheName = getCacheName(joinPoint);
        String sourceKey = getParams(joinPoint).get("sourceKey");
        try {
            Source source = getSource(Objects.requireNonNull(sourceKey));
            AchillesCacheEntity cacheEntity = cacheService.getCache(Objects.requireNonNull(source), cacheName);
            if (Objects.isNull(cacheEntity)) {
                Object result = joinPoint.proceed();
                try {
                    cacheEntity = cacheService.createCache(source, cacheName, result);
                } catch (DataIntegrityViolationException e) {
                    // cache can be created during executing join point, try to get it again
                    cacheEntity = cacheService.getCache(Objects.requireNonNull(source), cacheName);
                }
            }

            return objectMapper.readValue(cacheEntity.getCache(), getReturnType(joinPoint));
        } catch (Exception e) {
            LOG.error("exception during getting cache " + cacheName + " for source " + sourceKey, e);
            // ignore exception and call join point
            return joinPoint.proceed();
        }
    }

    private Class<?> getReturnType(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        return ((MethodSignature) signature).getReturnType();
    }

    private Source getSource(String sourceKey) {
        return sourceRepository.findBySourceKey(sourceKey);
    }

    private String getAnnotationValue(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        return method.getAnnotation(AchillesCache.class).value();
    }

    private String getCacheName(JoinPoint joinPoint) {
        String cachePrefix = getAnnotationValue(joinPoint);
        Map<String, String> params = getParams(joinPoint);
        String paramNamePart = params.entrySet().stream()
                .filter(entry -> !"sourceKey".equals(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.joining("_"));
        return cachePrefix + (StringUtils.isEmpty(paramNamePart) ? "" : "_" + paramNamePart);
    }

    private Map<String, String> getParams(JoinPoint joinPoint) {
        String[] names = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Object[] objects = joinPoint.getArgs();
        return IntStream.range(0, names.length)
                .boxed()
                .collect(Collectors.toMap(i -> names[i],
                        i -> String.valueOf(objects[i])));
    }
}
