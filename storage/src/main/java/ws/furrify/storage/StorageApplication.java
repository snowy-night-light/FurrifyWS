package ws.furrify.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ws.furrify.core.ApplicationCore;

@SpringBootApplication(scanBasePackages = "ws.furrify")
@EnableJpaRepositories(basePackages = "ws.furrify.storage.domain")
public class StorageApplication extends ApplicationCore {

	static void main(String[] args) {
		SpringApplication.run(StorageApplication.class, args);
	}

}
