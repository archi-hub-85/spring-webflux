package ru.akh.spring_webflux.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

public class DebugInterceptor implements MethodInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(DebugInterceptor.class);

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Class<?> targetClass = AopUtils.getTargetClass(invocation.getThis());
        String className = targetClass.getName();
        String methodName = AopUtils.getMostSpecificMethod(invocation.getMethod(), targetClass).getName();
        logger.trace("Invoking {}#{}({})...", className, methodName, invocation.getArguments());

        try {
            Object result = invocation.proceed();
            logger.trace("Method {}#{}(...) returns {}", className, methodName, result);
            return result;
        } catch (Throwable ex) {
            logger.trace("Method {}#{}(...) throwns exception", className, methodName, ex);
            throw ex;
        }
    }

}
