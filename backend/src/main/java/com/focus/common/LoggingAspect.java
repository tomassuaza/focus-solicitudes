package com.focus.common;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Log de entrada/salida de controllers (observabilidad TRD §7.5).
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("within(com.focus..*Controller)")
    public Object logController(ProceedingJoinPoint pjp) throws Throwable {
        long t0 = System.currentTimeMillis();
        String method = pjp.getSignature().toShortString();
        try {
            Object result = pjp.proceed();
            log.info("[{}] OK en {} ms", method, System.currentTimeMillis() - t0);
            return result;
        } catch (Throwable ex) {
            log.warn("[{}] FALLO ({}) en {} ms", method, ex.getClass().getSimpleName(),
                System.currentTimeMillis() - t0);
            throw ex;
        }
    }
}
