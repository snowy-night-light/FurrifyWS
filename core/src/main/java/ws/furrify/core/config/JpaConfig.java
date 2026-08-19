package ws.furrify.core.config;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;

@EnableTransactionManagement
@EnableJpaRepositories
@EnableJpaAuditing
public abstract class JpaConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean(initMethod = "start", destroyMethod = "stop")
    @Profile("dev")
    public Server h2TcpServer() throws SQLException {
        int h2Port = serverPort + 10000;
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", String.valueOf(h2Port));
    }

    @Bean
    @Profile("dev")
    public DataSourceInitializer devDataSourceInitializer(DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("db/init.sql"));

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);

        return initializer;
    }

}
