package com.gap.api;

import com.gap.api.Integration.PostgresTestContainerConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgresTestContainerConfiguration.class)
class ApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
