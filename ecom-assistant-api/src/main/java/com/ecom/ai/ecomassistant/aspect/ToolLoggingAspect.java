package com.ecom.ai.ecomassistant.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class ToolLoggingAspect {

    @Before("@annotation(toolAnnotation)")
    public void logToolCall(JoinPoint joinPoint, Tool toolAnnotation) {
        String message = "Tool method called: " + joinPoint.getSignature().getName()
                + " with args: " + Arrays.toString(joinPoint.getArgs());

        log.info(message);
    }

    @AfterReturning(pointcut = "@annotation(toolAnnotation)", returning = "result")
    public void logToolReturn(JoinPoint joinPoint, Tool toolAnnotation, Object result) {
        String message = "Tool method " + joinPoint.getSignature().getName()
                + " returned: " + result;

        log.info(message);
    }
}
