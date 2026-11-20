package ar.edu.utn.frc.dsi.bolsines;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

@SpringBootApplication
public class BolsinesApplication implements WebMvcConfigurer {

    @Value("${app.cors.origins}")
    private String[] origenesCors;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BolsinesApplication.class);
        app.addListeners(new CreadorBaseDatos()); // crea la base "bolsines" si no existe
        app.run(args);
    }

    /** Cliente HTTP compartido por los servicios de los 3 modelos de GPS Tracker. */
    @Bean
    RestClient gpsRestClient(@Value("${gps.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(origenesCors).allowedMethods("*").allowedHeaders("*");
    }
}
