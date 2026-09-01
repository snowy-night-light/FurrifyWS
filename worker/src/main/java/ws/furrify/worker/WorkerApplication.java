package ws.furrify.worker;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ws.furrify.core.ApplicationCore;
import ws.furrify.core.service.ExternalPluginLoaderService;
import ws.furrify.worker.shared.plugin.WorkerPluginIntf;

import java.util.List;

@SpringBootApplication(scanBasePackages = {"ws.furrify.worker", "ws.furrify.core"})
@EnableJpaRepositories(basePackages = "ws.furrify.worker.domain")
@RequiredArgsConstructor
public class WorkerApplication extends ApplicationCore implements CommandLineRunner{

    private final ExternalPluginLoaderService externalPluginLoaderService;

    static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        List<WorkerPluginIntf> workerPluginIntfList = externalPluginLoaderService.getPlugins(WorkerPluginIntf.class);
        boolean hasDuplicates = workerPluginIntfList.stream()
                .map(WorkerPluginIntf::getProviderName)
                .distinct()
                .count() < workerPluginIntfList.size();

        if (hasDuplicates) {
            throw new IllegalStateException("Duplicate worker plugins found with same provider name! Unable to start.");
        }
    }
}
