package nl.fairspace.pluto.app.config.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "app")
@Configuration
@Data
public class AppConfig {
    private String sessionCookieName = "JSESSIONID";
    private boolean forceHttps = true;
}

