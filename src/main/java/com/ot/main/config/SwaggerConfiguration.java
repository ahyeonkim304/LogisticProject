package com.ot.main.config;

/**
 * SwaggerConfiguration (DEPRECATED).
 *
 * Springfox 2.9.2는 Spring Boot 2.5+ 와 호환되지 않아 의존성에서 제거하였습니다.
 * (Spring Boot 2.5.6 + Java 17 환경에서는 PathPatternMatchableHandlerMapping 충돌 발생)
 *
 * Swagger UI가 다시 필요한 경우 springdoc-openapi-ui 로 대체하는 것을 권장합니다.
 *
 *   <dependency>
 *     <groupId>org.springdoc</groupId>
 *     <artifactId>springdoc-openapi-ui</artifactId>
 *     <version>1.6.15</version>
 *   </dependency>
 */
public class SwaggerConfiguration {
}
