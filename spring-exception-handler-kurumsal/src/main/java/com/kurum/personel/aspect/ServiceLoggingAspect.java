package com.kurum.personel.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceLoggingAspect.class);

    @Around("@within(com.kurum.personel.aspect.LogServiceCall)")
    public Object logServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.nanoTime();
        String operation = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName();

        try {
            Object result = joinPoint.proceed();
            if (isReadOperation(joinPoint.getSignature().getName())) {
                log.debug("Servis işlemi tamamlandı. operation={}, durationMs={}",
                        operation, elapsedMillis(startTime));
            } else {
                log.info("Servis işlemi tamamlandı. operation={}, durationMs={}",
                        operation, elapsedMillis(startTime));
            }
            return result;
        } catch (Throwable ex) {
            log.debug("Servis işlemi başarısız. operation={}, exceptionType={}, durationMs={}",
                    operation, ex.getClass().getSimpleName(), elapsedMillis(startTime));
            throw ex;
        }
    }

    private boolean isReadOperation(String methodName) {
        return methodName.startsWith("get") || methodName.startsWith("search");
    }

    private long elapsedMillis(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }
}
