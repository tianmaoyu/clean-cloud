package org.clean.example.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.clean.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import brave.Span;
import brave.Tracer;

@Aspect
@Configuration
//@ConditionalOnBean(Tracer.class)
//@DependsOn("tracer")
public class ZipkinAuthorAspect {
    
    @Autowired(required = false)
    private Tracer tracer;

//  无法在方法体内获取到注解的实例了。所以，我们使用参数绑定的方式，将注解作为参数传递进来。因此，我们使用：  第二个方法
//    @Around("@within(Author) || @annotation(Author)")
//    public Object traceAuthor(ProceedingJoinPoint joinPoint) throws Throwable {
//        // 获取实际的 Author 注解（处理继承和覆盖）
//        Author methodAuthor = getAuthorAnnotation(joinPoint);
//
//        if (methodAuthor != null) {
//            Span currentSpan = tracer.currentSpan();
//            if (currentSpan != null) {
//                // 添加作者信息到 Zipkin span
//                currentSpan.tag("author.name", methodAuthor.value());
//                if (!methodAuthor.date().isEmpty()) {
//                    currentSpan.tag("author.date", methodAuthor.date());
//                }
//                // 添加自定义标签用于识别
//                currentSpan.tag("custom.annotation", "Author");
//            }
//        }
//
//        return joinPoint.proceed();
//    }

    @Around("@within(author) || @annotation(author)")
    public Object traceAuthor(ProceedingJoinPoint joinPoint, Author author) throws Throwable {

        if (tracer == null) {
            return joinPoint.proceed();
        }

        // 直接使用传入的 author 参数，它要么是方法上的注解，要么是类上的注解
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null && author != null) {
            currentSpan.tag("author.name", author.value());
            if (!author.date().isEmpty()) {
                currentSpan.tag("author.date", author.date());
            }
        }

        return joinPoint.proceed();



    }
    
    private Author getAuthorAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        
        // 先检查方法上的注解
        Author methodAnnotation = AnnotationUtils.findAnnotation(
            signature.getMethod(), Author.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        
        // 如果方法上没有，检查类上的注解
        Class<?> targetClass = joinPoint.getTarget().getClass();
        return AnnotationUtils.findAnnotation(targetClass, Author.class);
    }
}