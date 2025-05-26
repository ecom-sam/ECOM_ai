package com.ecom.ai.ecomassistant.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class ToolLoggingAspect {

    @Before("@annotation(toolAnnotation)")
    public void logToolCall(JoinPoint joinPoint, Tool toolAnnotation) {
        System.out.println("Tool method called: " + joinPoint.getSignature().getName()
                + " with args: " + Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "@annotation(toolAnnotation)", returning = "result")
    public void logToolReturn(JoinPoint joinPoint, Tool toolAnnotation, Object result) {
        System.out.println("Tool method " + joinPoint.getSignature().getName() + " returned: " + result);
    }
}
