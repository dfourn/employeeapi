package com.reliaquest.api;

import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.reliaquest.api..*(..))")
    public void loggableMethods() {}

    @Around("loggableMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        logger.debug("Entering: {} with arguments {}", methodName, Arrays.toString(args));
        Object result = joinPoint.proceed();
        logger.debug("Exiting: {} with result {}", methodName, result);
        return result;
    }
}
