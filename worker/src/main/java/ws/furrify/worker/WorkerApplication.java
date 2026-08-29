package ws.furrify.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"ws.furrify.worker", "ws.furrify.core"})
@EnableJpaRepositories(basePackages = "ws.furrify.worker.domain")
public class WorkerApplication {

    static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }

}
