package com.logis.common.config;

import com.logis.common.interceptor.AdminInterceptor;
import com.logis.common.interceptor.LoginInterceptor;
import com.logis.common.interceptor.PageLoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // REST API 로그인 체크 (401 JSON 응답)
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/me");

        // REST API 관리자 체크 (403 JSON 응답)
        registry.addInterceptor(new AdminInterceptor())
                .addPathPatterns("/api/admin/**");

        // 페이지 라우트 로그인 체크 (/login 으로 리다이렉트)
        registry.addInterceptor(new PageLoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login", "/register",
                        "/api/**",
                        "/css/**", "/js/**", "/images/**",
                        "/error");
    }
}
