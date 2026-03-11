package com.billMate.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = "invoice.created")
@TestPropertySource(properties = {
		"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"spring.kafka.consumer.auto-offset-reset=earliest"
})
class NotificationServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
