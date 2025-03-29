package com.dumbqr.dumbqr.service;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Supplier;

@Component
@Aspect
public class RateLimiterAspect {

    private final ProxyManager<String> proxyManager;
    private final Supplier<BucketConfiguration> bucketConfigurationSupplier;

    public RateLimiterAspect(ProxyManager<String> proxyManager, Supplier<BucketConfiguration> bucketConfigurationSupplier) {
        this.proxyManager = proxyManager;
        this.bucketConfigurationSupplier = bucketConfigurationSupplier;
    }

    @Around("@annotation(RateLimited)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        // Determine the client IP: first check for X-Forwarded-For header (if behind a proxy) then fallback
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }

        Bucket bucket = getOrCreateBucket(clientIp);

        if (!bucket.tryConsume(1)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
        }
        return joinPoint.proceed();
    }

    private Bucket getOrCreateBucket(String key) {
        return proxyManager.builder()
                .build(key, bucketConfigurationSupplier.get());
    }
}
