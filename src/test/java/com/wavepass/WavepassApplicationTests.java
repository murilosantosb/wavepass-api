package com.wavepass;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"spring.datasource.url=jdbc:h2:mem:startupcheck",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"api.security.token.secret=local-startup-test-secret",
		"spring.ai.openai.api-key=local-startup-test-key"
})
class WavepassApplicationTests {

	@Test
	void contextLoads() {
	}

}
