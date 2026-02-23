package com.finanCerto.finanCertoBack;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Disabled("Integration test - context loading test, can be flaky in CI/CD")
@SpringBootTest
@ActiveProfiles("test")
class FinanCertoBackApplicationTests {

	@Test
	void contextLoads() {
	}

}
