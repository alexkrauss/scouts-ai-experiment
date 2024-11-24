package name.alexkrauss.scouts.infrastructure.db;

import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JooqConfig {
    @Bean
    public DefaultConfigurationCustomizer jooqConfigurationCustomizer() {
        return config -> config.settings()
                             .withExecuteWithOptimisticLocking(true);
    }
}
