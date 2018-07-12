package com.jnj.honeur.webapi.aspect;

import com.google.common.collect.Lists;
import com.jnj.honeur.webapi.SourceDaimonContextHolder;
import com.jnj.honeur.webapi.source.SourceDaimonContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Aspect that sets the correct @{@link SourceDaimonContext}
 *
 * @author Peter Moorthamer
 */

@Aspect
@Component
@Order(Integer.MIN_VALUE)
public class SourceDaimonContextAspect {

    private static final Log LOG = LogFactory.getLog(SourceDaimonContextAspect.class);

    private static final String SOURCE_KEY_PARAM = "sourceKey";

    @Pointcut("target(org.ohdsi.webapi.service.AbstractDaoService)")
    public void inAbstractDaoService() {}

    @Pointcut("execution(* *.import*(..)) && target(org.ohdsi.webapi.service.AbstractDaoService)")
    public void inAbstractDaoServiceImport() {}

    @Pointcut("execution(* *.create*(..)) && target(org.ohdsi.webapi.service.AbstractDaoService)")
    public void inAbstractDaoServiceCreate() {}

    @Pointcut("execution(* *.save*(..)) && target(org.ohdsi.webapi.service.AbstractDaoService)")
    public void inAbstractDaoServiceSave() {}

    @Pointcut("inAbstractDaoServiceImport() || inAbstractDaoServiceCreate() || inAbstractDaoServiceSave()")
    public void inAbstractDaoServiceModify(){}

    @Pointcut("execution(* *.importCohortResults(..)) && target(org.ohdsi.webapi.service.AbstractDaoService)")
    public void inAbstractDaoServiceImportCohortResults(){}

    /**
     * Set the SourceDaimonContext before executing the operation and clear the context afterwards
     */
    @Around("inAbstractDaoServiceImportCohortResults()")
    public Object setSourceDaimonContext(ProceedingJoinPoint jp) throws DataAccessException {
        LOG.debug("!!!!!!!!!!!!! setSourceDaimonContext !!!!!!!!!!!!!!");
        LOG.debug(jp.getTarget().getClass().getSimpleName() + " "
                + jp.toShortString() + " " + Arrays.toString(jp.getArgs()));

        Object result;

//        String sourceKey = getSourceKeyValue(jp);
//        if(StringUtils.isNotBlank(sourceKey)) {
//            SourceDaimonContextHolder.setCurrentSourceDaimonContext(new SourceDaimonContext(sourceKey, SourceDaimon.DaimonType.Results));
//        }

        // Proceed
        try {
            result = jp.proceed();
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
            throw new DataAccessResourceFailureException(String.format("Data access exception: %s", t.getMessage()), t);
        } finally {
            SourceDaimonContextHolder.clear();
        }

        return result;
    }

    /**
     * Read the sourceKey value from the sourceKey parameter
     * @param jp the join point to retrieve the sourceKey parameter name and value from
     * @return the value of the sourceKey parameter
     */
    private String getSourceKeyValue(final ProceedingJoinPoint jp) {
        final MethodSignature methodSignature = (MethodSignature)jp.getSignature();
        String[] parameterNames = methodSignature.getParameterNames();
        Object[] parameterValues = jp.getArgs();
        return getsSourceKeyValue(parameterNames, parameterValues);
    }

    /**
     * Read the sourceKey value from the sourceKey parameter
     * @param parameterNames String array of parameter names
     * @param parameterValues Object array of parameter values
     * @return the value of the sourceKey parameter
     */
    private String getsSourceKeyValue(String[] parameterNames, Object[] parameterValues) {
        if(parameterNames == null || parameterNames.length == 0) {
            return null;
        }
        List parameterNameList = Lists.newArrayList(parameterNames);
        int index = parameterNameList.indexOf(SOURCE_KEY_PARAM);
        if(index < 0) {
            return null;
        }
        Object parameterValue = parameterValues[index];
        if(parameterValue instanceof String) {
            return (String)parameterValue;
        }
        return null;
    }

}
