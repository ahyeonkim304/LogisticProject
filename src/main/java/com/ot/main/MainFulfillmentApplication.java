package com.ot.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 메인 진입점.
 *
 * - 패키징을 war 로 변경했기 때문에 외부 톰캣에 배포할 수 있도록
 *   SpringBootServletInitializer 를 확장합니다.
 * - 임베디드 톰캣 (java -jar / mvn spring-boot:run) 으로도 그대로 동작합니다.
 */
@SpringBootApplication
public class MainFulfillmentApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(MainFulfillmentApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(MainFulfillmentApplication.class, args);
	}

}
