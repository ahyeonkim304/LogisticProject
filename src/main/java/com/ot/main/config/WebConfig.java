package com.ot.main.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 정적 리소스 (CSS, JS, 이미지) 매핑과 루트 경로 리다이렉트를 처리한다.
 *
 *  - /static/**       -> classpath:/static/   (CSS / JS / image 파일들)
 *  - /css/**          -> classpath:/static/css/
 *  - /js/**           -> classpath:/static/js/
 *  - /assets/**       -> classpath:/static/assets/
 *  - /                -> 로그인 페이지로 리다이렉트
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/assets/**").addResourceLocations("classpath:/static/assets/");
        registry.addResourceHandler("/data/**").addResourceLocations("classpath:/static/data/");
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 루트 경로 → 관리자 로그인 페이지로 이동
        registry.addRedirectViewController("/", "/api/v1/main-fulfillment/showLogin");
    }
}
