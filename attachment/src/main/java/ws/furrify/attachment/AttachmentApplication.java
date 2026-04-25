package ws.furrify.attachment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ws.furrify.core.ApplicationCore;

@SpringBootApplication(scanBasePackages = "ws.furrify")
@EnableJpaRepositories(basePackages = "ws.furrify.attachment.domain")
public class AttachmentApplication extends ApplicationCore {

    static void main(String[] args) {
        SpringApplication.run(AttachmentApplication.class, args);
    }

}
