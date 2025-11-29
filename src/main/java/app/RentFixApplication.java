package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "app.feign")
@EnableScheduling
@EnableCaching
public class RentFixApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentFixApplication.class, args);
	}

}
