package com.clawdash.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("within(com.clawdash.controller..*)")
    public void controllerPointcut() {}

    @Pointcut("within(com.clawdash.service..*)")
    public void servicePointcut() {}

    @Around("controllerPointcut()")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String method = request.getMethod();
            String uri = request.getRequestURI();
            
            log.info(">>> {} {}", method, uri);
            
            long startTime = System.currentTimeMillis();
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("<<< {} {} - {}ms", method, uri, duration);
            
            return result;
        }
        
        return joinPoint.proceed();
    }

    @AfterThrowing(pointcut = "controllerPointcut() || servicePointcut()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Exception exception) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String args = Arrays.toString(joinPoint.getArgs());
        
        log.error("!!! Exception in {}.{}() with args: {}", className, methodName, args, exception);
    }
}
