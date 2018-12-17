package nl.fairspace.pluto.app.config;

import nl.fairspace.pluto.app.config.dto.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import java.util.HashMap;

@Configuration
@EnableSpringHttpSession
public class SessionConfiguration {
    @Autowired
    AppConfig appConfig;

    @Bean
    public MapSessionRepository sessionRepository() {
        return new MapSessionRepository(new HashMap<>());
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName(appConfig.getSessionCookieName());
        serializer.setCookiePath("/");

        // Use the full hostname as domain name pattern, if it is not an ip address
        serializer.setDomainNamePattern("^((?:\\w+\\.)+[a-z]+)$");
        return serializer;
    }
}
