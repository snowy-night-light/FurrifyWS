package ws.furrify.attachment.config;

import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ws.furrify.attachment.service.file.storage.FileMassStorageStrategy;
import ws.furrify.attachment.service.file.storage.HostMountpointV1FileMassStorageStrategy;

@Configuration
class FileMassStorageConfig {

    @Bean
    public FileMassStorageStrategy fileMassStorageService() {
        return new HostMountpointV1FileMassStorageStrategy();
    }

    @Bean
    public Tika tika() {
        return new Tika();
    }
}
