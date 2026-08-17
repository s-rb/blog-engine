package main.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String[] ALLOWED_METHODS = {"GET", "POST", "PUT", "DELETE"};
    private static final String[] ALLOWED_ORIGINS = {
            "http://localhost:3000",
            "http://blog.surkoff.com",
            "https://blog.surkoff.com",
            "http://surkoff.com",
            "https://surkoff.com"
    };

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(ALLOWED_ORIGINS)
                .allowedMethods(ALLOWED_METHODS)
                .allowedHeaders("Authorization", "Content-Type")
                .allowCredentials(true);
    }
}
