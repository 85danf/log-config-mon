package org.danf.configmon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Dan Feldman
 */
@Configuration
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
@EnableJpaRepositories
@SpringBootApplication
public class ConfigMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigMonitorApplication.class, args);
	}
}
